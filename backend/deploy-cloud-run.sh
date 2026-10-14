#!/bin/bash
# =========================================================================
# UPISmart 2.0 — Google Cloud SQL & Cloud Run Automated Deployment Script
# Execute this in Google Cloud Shell
# =========================================================================

set -e

PROJECT_ID=$(gcloud config get-value project)
REGION="asia-south1"
INSTANCE_NAME="upismart-mysql-instance"
DB_NAME="upismart_db"
DB_USER="upismart_user"
SERVICE_NAME="upismart-backend"

echo "=========================================================="
echo " Deploying UPISmart 2.0 Full-Stack to Google Cloud"
echo " Project ID: $PROJECT_ID"
echo " Region:     $REGION"
echo "=========================================================="

# 1. Enable required Google Cloud APIs
echo "\n--> Enabling Cloud Run, Cloud Build, and Cloud SQL Admin APIs..."
gcloud services enable \
    run.googleapis.com \
    cloudbuild.googleapis.com \
    sqladmin.googleapis.com

# 2. Check or create Google Cloud SQL MySQL Instance
if ! gcloud sql instances describe $INSTANCE_NAME &>/dev/null; then
    echo "\n--> Provisioning Google Cloud SQL MySQL 8.0 instance (this may take a few minutes)..."
    gcloud sql instances create $INSTANCE_NAME \
        --database-version=MYSQL_8_0 \
        --tier=db-f1-micro \
        --region=$REGION \
        --root-password=RootPasswordDemo123!
else
    echo "\n--> Cloud SQL instance '$INSTANCE_NAME' already exists."
fi

# 3. Create database
echo "\n--> Ensuring database '$DB_NAME' exists..."
gcloud sql databases create $DB_NAME --instance=$INSTANCE_NAME || true

# 4. Create user
echo "\n--> Configuring application database user..."
gcloud sql users create $DB_USER \
    --instance=$INSTANCE_NAME \
    --password="DemoPassword123!" || true

# 5. Import relational schema into Cloud SQL
echo "\n--> Executing MySQL schema DDL (backend/schema.sql)..."
gcloud sql import sql $INSTANCE_NAME gs://${PROJECT_ID}-sql-imports/schema.sql --database=$DB_NAME || echo "Notice: You can also run 'gcloud sql connect $INSTANCE_NAME --user=root < backend/schema.sql'"

# 6. Build and Deploy container to Google Cloud Run
echo "\n--> Submitting container build to Google Cloud Build..."
gcloud builds submit --tag gcr.io/$PROJECT_ID/$SERVICE_NAME ./backend

CONNECTION_NAME=$(gcloud sql instances describe $INSTANCE_NAME --format="value(connectionName)")

echo "\n--> Deploying to Google Cloud Run with Cloud SQL connection..."
gcloud run deploy $SERVICE_NAME \
    --image gcr.io/$PROJECT_ID/$SERVICE_NAME \
    --platform managed \
    --region $REGION \
    --add-cloudsql-instances $CONNECTION_NAME \
    --set-env-vars "SPRING_PROFILES_ACTIVE=prod,DB_NAME=$DB_NAME,DB_USER=$DB_USER,DB_PASS=DemoPassword123!,CLOUD_SQL_CONNECTION_NAME=$CONNECTION_NAME" \
    --allow-unauthenticated

SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --platform managed --region $REGION --format="value(status.url)")

echo "\n=========================================================="
echo " UPISmart 2.0 Backend & Web Dashboard Deployed Successfully!"
echo " Public Cloud Run URL: $SERVICE_URL"
echo " Web Dashboard:        $SERVICE_URL/"
echo " REST API Health:      $SERVICE_URL/api/health"
echo "=========================================================="
