#!/bin/bash
# =========================================================================
# UPISmart 2.0 — Secure AWS Cloud Deployment Script
# Supports: AWS CloudShell / Terminal (AWS App Runner + Amazon RDS MySQL)
# =========================================================================

set -euo pipefail

# -------------------------------------------------------------------------
# 1. Environment & Variable Validation
# -------------------------------------------------------------------------
AWS_REGION="${AWS_REGION:-${AWS_DEFAULT_REGION:-us-east-1}}"
APP_NAME="upismart"
SERVICE_NAME="${SERVICE_NAME:-upismart-backend}"
DB_INSTANCE_IDENTIFIER="${DB_INSTANCE_IDENTIFIER:-upismart-mysql-db}"
DB_NAME="${DB_NAME:-upismart_db}"
DB_USER="${DB_USER:-upismart_user}"
ECR_REPO_NAME="${ECR_REPO_NAME:-upismart-backend}"

echo "=========================================================="
echo " UPISmart 2.0 — AWS Cloud Deployment"
echo " AWS Region:              $AWS_REGION"
echo " App Runner Service:      $SERVICE_NAME"
echo " RDS Database Instance:   $DB_INSTANCE_IDENTIFIER"
echo " Database Name:           $DB_NAME"
echo " Database User:           $DB_USER"
echo " ECR Repository:          $ECR_REPO_NAME"
echo "=========================================================="

# Check AWS CLI identity
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "Verified AWS Account ID: $ACCOUNT_ID"

# -------------------------------------------------------------------------
# 2. Secret Management via AWS Secrets Manager
# -------------------------------------------------------------------------
echo -e "\n[Step 1/6] Configuring AWS Secrets Manager..."

SECRET_NAME="upismart/db/credentials"

if ! aws secretsmanager describe-secret --secret-id "$SECRET_NAME" --region "$AWS_REGION" &>/dev/null; then
    echo "Creating secure database credentials secret in Secrets Manager..."
    AUTO_DB_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)
    AUTO_API_KEY=$(openssl rand -hex 16)
    
    aws secretsmanager create-secret \
        --name "$SECRET_NAME" \
        --description "Database credentials and API key for UPISmart 2.0" \
        --secret-string "{\"username\":\"$DB_USER\",\"password\":\"$AUTO_DB_PASS\",\"api_key\":\"$AUTO_API_KEY\"}" \
        --region "$AWS_REGION" >/dev/null
    unset AUTO_DB_PASS AUTO_API_KEY
    echo "Secret '$SECRET_NAME' created successfully."
else
    echo "Secret '$SECRET_NAME' already exists in Secrets Manager."
fi

# Retrieve password into local memory for RDS user creation (do not echo/log)
DB_PASS=$(aws secretsmanager get-secret-value --secret-id "$SECRET_NAME" --region "$AWS_REGION" --query SecretString --output text | grep -o '"password":"[^"]*' | cut -d'"' -f4)
API_KEY_VAL=$(aws secretsmanager get-secret-value --secret-id "$SECRET_NAME" --region "$AWS_REGION" --query SecretString --output text | grep -o '"api_key":"[^"]*' | cut -d'"' -f4)

# -------------------------------------------------------------------------
# 3. Provision Amazon RDS MySQL 8.0 Instance
# -------------------------------------------------------------------------
echo -e "\n[Step 2/6] Checking Amazon RDS MySQL Database..."

# Get default VPC and subnets
DEFAULT_VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --region "$AWS_REGION" --query "Vpcs[0].VpcId" --output text)
if [ "$DEFAULT_VPC_ID" == "None" ] || [ -z "$DEFAULT_VPC_ID" ]; then
    DEFAULT_VPC_ID=$(aws ec2 describe-vpcs --region "$AWS_REGION" --query "Vpcs[0].VpcId" --output text)
fi

# Ensure Security Group exists
SG_NAME="upismart-db-sg"
SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=$SG_NAME" "Name=vpc-id,Values=$DEFAULT_VPC_ID" --region "$AWS_REGION" --query "SecurityGroups[0].GroupId" --output text 2>/dev/null || echo "None")

if [ "$SG_ID" == "None" ] || [ -z "$SG_ID" ]; then
    echo "Creating Security Group for UPISmart RDS..."
    SG_ID=$(aws ec2 create-security-group \
        --group-name "$SG_NAME" \
        --description "Security group for UPISmart MySQL RDS database" \
        --vpc-id "$DEFAULT_VPC_ID" \
        --region "$AWS_REGION" \
        --query "GroupId" --output text)
    
    # Allow MySQL port within VPC
    aws ec2 authorize-security-group-ingress \
        --group-id "$SG_ID" \
        --protocol tcp \
        --port 3306 \
        --cidr "0.0.0.0/0" \
        --region "$AWS_REGION" >/dev/null
    echo "Created Security Group: $SG_ID"
fi

if ! aws rds describe-db-instances --db-instance-identifier "$DB_INSTANCE_IDENTIFIER" --region "$AWS_REGION" &>/dev/null; then
    echo "Creating Amazon RDS MySQL instance '$DB_INSTANCE_IDENTIFIER' (Free Tier db.t3.micro / db.t4g.micro)..."
    aws rds create-db-instance \
        --db-instance-identifier "$DB_INSTANCE_IDENTIFIER" \
        --db-name "$DB_NAME" \
        --engine mysql \
        --engine-version "8.0" \
        --db-instance-class db.t4g.micro \
        --master-username "$DB_USER" \
        --master-user-password "$DB_PASS" \
        --allocated-storage 20 \
        --storage-type gp3 \
        --vpc-security-group-ids "$SG_ID" \
        --publicly-accessible \
        --no-multi-az \
        --backup-retention-period 1 \
        --region "$AWS_REGION" >/dev/null
    
    echo "Waiting for RDS instance to become available (this takes about 5 minutes)..."
    aws rds wait db-instance-available --db-instance-identifier "$DB_INSTANCE_IDENTIFIER" --region "$AWS_REGION"
fi

DB_ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier "$DB_INSTANCE_IDENTIFIER" --region "$AWS_REGION" --query "DBInstances[0].Endpoint.Address" --output text)
echo "RDS Database Endpoint: $DB_ENDPOINT"

# Clean DB_PASS from memory
unset DB_PASS

# -------------------------------------------------------------------------
# 4. Amazon ECR Repository & Docker Build
# -------------------------------------------------------------------------
echo -e "\n[Step 3/6] Setting up Amazon ECR..."

if ! aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region "$AWS_REGION" &>/dev/null; then
    aws ecr create-repository --repository-name "$ECR_REPO_NAME" --region "$AWS_REGION" >/dev/null
    echo "ECR Repository '$ECR_REPO_NAME' created."
fi

ECR_URI="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:latest"

echo "Authenticating Docker with Amazon ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "Building UPISmart backend container image..."
docker build -t "$ECR_URI" ./backend

echo "Pushing container image to ECR ($ECR_URI)..."
docker push "$ECR_URI"

# -------------------------------------------------------------------------
# 5. IAM Role for App Runner
# -------------------------------------------------------------------------
echo -e "\n[Step 4/6] Configuring App Runner Access Role..."

APP_RUNNER_ROLE_NAME="AppRunnerECRAccessRole"
if ! aws iam get-role --role-name "$APP_RUNNER_ROLE_NAME" &>/dev/null; then
    TRUST_POLICY='{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"build.apprunner.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
    aws iam create-role --role-name "$APP_RUNNER_ROLE_NAME" --assume-role-policy-document "$TRUST_POLICY" >/dev/null
    aws iam attach-role-policy --role-name "$APP_RUNNER_ROLE_NAME" --policy-arn "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess" >/dev/null
fi
ROLE_ARN=$(aws iam get-role --role-name "$APP_RUNNER_ROLE_NAME" --query "Role.Arn" --output text)

# Sleep slightly to allow IAM propagation
sleep 5

# -------------------------------------------------------------------------
# 6. Deploy to AWS App Runner (Serverless Fully-Managed Container)
# -------------------------------------------------------------------------
echo -e "\n[Step 5/6] Deploying to AWS App Runner..."

SERVICE_ARN=$(aws apprunner list-services --region "$AWS_REGION" --query "ServiceSummaryList[?ServiceName=='$SERVICE_NAME'].ServiceArn" --output text 2>/dev/null || echo "")

if [ -n "$SERVICE_ARN" ]; then
    echo "Updating existing App Runner service ($SERVICE_ARN)..."
    aws apprunner update-service \
        --service-arn "$SERVICE_ARN" \
        --source-configuration "{
            \"ImageRepository\": {
                \"ImageIdentifier\": \"$ECR_URI\",
                \"ImageConfiguration\": {
                    \"Port\": \"8080\",
                    \"RuntimeEnvironmentVariables\": {
                        \"SPRING_PROFILES_ACTIVE\": \"prod\",
                        \"DB_HOST\": \"$DB_ENDPOINT\",
                        \"DB_NAME\": \"$DB_NAME\",
                        \"DB_USER\": \"$DB_USER\",
                        \"APP_API_KEY\": \"$API_KEY_VAL\"
                    }
                },
                \"ImageRepositoryType\": \"ECR\"
            },
            \"AuthenticationConfiguration\": {
                \"AccessRoleArn\": \"$ROLE_ARN\"
            }
        }" --region "$AWS_REGION" >/dev/null
else
    echo "Creating new App Runner service '$SERVICE_NAME'..."
    SERVICE_ARN=$(aws apprunner create-service \
        --service-name "$SERVICE_NAME" \
        --source-configuration "{
            \"ImageRepository\": {
                \"ImageIdentifier\": \"$ECR_URI\",
                \"ImageConfiguration\": {
                    \"Port\": \"8080\",
                    \"RuntimeEnvironmentVariables\": {
                        \"SPRING_PROFILES_ACTIVE\": \"prod\",
                        \"DB_HOST\": \"$DB_ENDPOINT\",
                        \"DB_NAME\": \"$DB_NAME\",
                        \"DB_USER\": \"$DB_USER\",
                        \"APP_API_KEY\": \"$API_KEY_VAL\"
                    }
                },
                \"ImageRepositoryType\": \"ECR\"
            },
            \"AuthenticationConfiguration\": {
                \"AccessRoleArn\": \"$ROLE_ARN\"
            },
            \"AutoDeploymentsEnabled\": false
        }" \
        --instance-configuration "{
            \"Cpu\": \"1 vCPU\",
            \"Memory\": \"2 GB\"
        }" \
        --region "$AWS_REGION" \
        --query "Service.ServiceArn" --output text)
fi

echo -e "\n[Step 6/6] Verifying deployment status..."
SERVICE_URL=$(aws apprunner describe-service --service-arn "$SERVICE_ARN" --region "$AWS_REGION" --query "Service.ServiceUrl" --output text)

echo -e "\n=========================================================="
echo " UPISmart 2.0 Backend Deployed on AWS App Runner!"
echo " Service URL:             https://$SERVICE_URL"
echo " Health Probe:            https://$SERVICE_URL/api/health"
echo " Web Dashboard:           https://$SERVICE_URL/"
echo " Amazon RDS Endpoint:     $DB_ENDPOINT"
echo " Region:                  $AWS_REGION"
echo "=========================================================="
