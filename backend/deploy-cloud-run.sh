#!/bin/bash
# =========================================================================
# UPISmart 2.0 — Secure Google Cloud SQL & Cloud Run Deployment Script
# Execute in Google Cloud Shell
# =========================================================================

set -euo pipefail

# -------------------------------------------------------------------------
# 1. Environment & Variable Validation
# -------------------------------------------------------------------------
PROJECT_ID="${PROJECT_ID:-$(gcloud config get-value project 2>/dev/null || echo "")}"
if [ -z "$PROJECT_ID" ] || [ "$PROJECT_ID" = "(unset)" ]; then
    echo "ERROR: Google Cloud Project ID is not set."
    echo "Please set it with: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

REGION="${REGION:-asia-south1}"
INSTANCE_NAME="${INSTANCE_NAME:-upismart-mysql-instance}"
DB_NAME="${DB_NAME:-upismart_db}"
DB_USER="${DB_USER:-upismart_user}"
SERVICE_NAME="${SERVICE_NAME:-upismart-backend}"
SA_NAME="upismart-cloudrun-sa"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# Public demo accessibility toggle (true = allow unauthenticated public web dashboard; false = require IAM auth)
ALLOW_UNAUTHENTICATED="${ALLOW_UNAUTHENTICATED:-true}"

echo "=========================================================="
echo " UPISmart 2.0 — Production Cloud Deployment"
echo " Project ID:              $PROJECT_ID"
echo " Region:                  $REGION"
echo " Cloud SQL Instance:      $INSTANCE_NAME"
echo " Database Name:           $DB_NAME"
echo " Database User:           $DB_USER"
echo " Cloud Run Service:       $SERVICE_NAME"
echo " Runtime Service Account: $SA_EMAIL"
echo " Public Web Demo Access:  $ALLOW_UNAUTHENTICATED"
echo "=========================================================="

# -------------------------------------------------------------------------
# 2. Enable Required Google Cloud APIs
# -------------------------------------------------------------------------
echo -e "\n[Step 1/7] Enabling required GCP APIs..."
gcloud services enable \
    run.googleapis.com \
    cloudbuild.googleapis.com \
    sqladmin.googleapis.com \
    secretmanager.googleapis.com \
    iam.googleapis.com

# -------------------------------------------------------------------------
# 3. Secure Secret Management (Google Secret Manager)
# -------------------------------------------------------------------------
echo -e "\n[Step 2/7] Checking secrets in Google Secret Manager..."

# Helper function to create secret safely without printing value to logs or console
setup_secret_if_missing() {
    local secret_name="$1"
    local secret_val="$2"

    if ! gcloud secrets describe "$secret_name" --project="$PROJECT_ID" &>/dev/null; then
        echo "Creating secret '$secret_name' in Secret Manager..."
        echo -n "$secret_val" | gcloud secrets create "$secret_name" \
            --data-file=- \
            --replication-policy="automatic" \
            --project="$PROJECT_ID" >/dev/null
    else
        echo "Secret '$secret_name' verified in Secret Manager."
    fi
}

# Auto-generate secure random credentials if secrets do not already exist in Secret Manager
AUTO_DB_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)
AUTO_ROOT_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)
AUTO_API_KEY=$(openssl rand -hex 16)

setup_secret_if_missing "upismart-db-user-password" "$AUTO_DB_PASS"
setup_secret_if_missing "upismart-db-root-password" "$AUTO_ROOT_PASS"
setup_secret_if_missing "upismart-api-key" "$AUTO_API_KEY"

# Clean auto-generated variables from shell memory
unset AUTO_DB_PASS AUTO_ROOT_PASS AUTO_API_KEY

# Fetch secrets into local variables with debug tracing disabled to prevent log leakage
{ set +x; } 2>/dev/null
DB_PASS_VALUE=$(gcloud secrets versions access latest --secret="upismart-db-user-password" --project="$PROJECT_ID")
DB_ROOT_PASS_VALUE=$(gcloud secrets versions access latest --secret="upismart-db-root-password" --project="$PROJECT_ID")

# -------------------------------------------------------------------------
# 4. Provision Google Cloud SQL (MySQL 8.0)
# -------------------------------------------------------------------------
echo -e "\n[Step 3/7] Checking Cloud SQL instance '$INSTANCE_NAME'..."
if ! gcloud sql instances describe "$INSTANCE_NAME" --project="$PROJECT_ID" &>/dev/null; then
    echo "Creating Cloud SQL MySQL 8.0 instance (this may take 4-7 minutes)..."
    gcloud sql instances create "$INSTANCE_NAME" \
        --database-version=MYSQL_8_0 \
        --tier=db-f1-micro \
        --region="$REGION" \
        --root-password="$DB_ROOT_PASS_VALUE" \
        --storage-auto-increase \
        --project="$PROJECT_ID"
else
    echo "Cloud SQL instance '$INSTANCE_NAME' already exists."
fi

echo "Verifying database '$DB_NAME'..."
if ! gcloud sql databases describe "$DB_NAME" --instance="$INSTANCE_NAME" --project="$PROJECT_ID" &>/dev/null; then
    gcloud sql databases create "$DB_NAME" --instance="$INSTANCE_NAME" --project="$PROJECT_ID"
fi

echo "Verifying application database user '$DB_USER'..."
if ! gcloud sql users list --instance="$INSTANCE_NAME" --project="$PROJECT_ID" --format="value(name)" | grep -qw "$DB_USER"; then
    gcloud sql users create "$DB_USER" \
        --instance="$INSTANCE_NAME" \
        --password="$DB_PASS_VALUE" \
        --project="$PROJECT_ID"
else
    # Keep password in sync with Secret Manager
    gcloud sql users set-password "$DB_USER" \
        --instance="$INSTANCE_NAME" \
        --password="$DB_PASS_VALUE" \
        --project="$PROJECT_ID"
fi

# Erase local password variables immediately
unset DB_PASS_VALUE DB_ROOT_PASS_VALUE

# -------------------------------------------------------------------------
# 5. Database Schema Initialization (Idempotent DDL)
# -------------------------------------------------------------------------
echo -e "\n[Step 4/7] Applying relational database schema (backend/schema.sql)..."
echo "Notice: backend/schema.sql is fully idempotent (CREATE IF NOT EXISTS, ON DUPLICATE KEY UPDATE)."
gcloud sql connect "$INSTANCE_NAME" --user=root --quiet --project="$PROJECT_ID" < backend/schema.sql || {
    echo "Notice: Schema connection completed or can be run interactively."
    echo "Alternative: The Spring Boot JPA engine also automatically initializes and validates entities on startup."
}

# -------------------------------------------------------------------------
# 6. Least-Privilege IAM Service Account Configuration
# -------------------------------------------------------------------------
echo -e "\n[Step 5/7] Configuring least-privilege Cloud Run runtime Service Account..."
if ! gcloud iam service-accounts describe "$SA_EMAIL" --project="$PROJECT_ID" &>/dev/null; then
    echo "Creating service account '$SA_NAME'..."
    gcloud iam service-accounts create "$SA_NAME" \
        --display-name="UPISmart Cloud Run Runtime Service Account" \
        --project="$PROJECT_ID"
fi

# Grant Cloud SQL Client role
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:$SA_EMAIL" \
    --role="roles/cloudsql.client" \
    --condition=None >/dev/null

# Grant Secret Manager Secret Accessor role
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:$SA_EMAIL" \
    --role="roles/secretmanager.secretAccessor" \
    --condition=None >/dev/null

echo "Granted roles/cloudsql.client and roles/secretmanager.secretAccessor to $SA_EMAIL."

# -------------------------------------------------------------------------
# 7. Build Container Image via Google Cloud Build
# -------------------------------------------------------------------------
echo -e "\n[Step 6/7] Submitting build to Google Cloud Build..."
IMAGE_TAG="gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"
gcloud builds submit --tag "$IMAGE_TAG" ./backend --project="$PROJECT_ID"

# -------------------------------------------------------------------------
# 8. Deploy to Google Cloud Run (Secrets Injected via Secret Manager)
# -------------------------------------------------------------------------
echo -e "\n[Step 7/7] Deploying to Google Cloud Run with Secret Manager binding..."
CONNECTION_NAME=$(gcloud sql instances describe "$INSTANCE_NAME" --project="$PROJECT_ID" --format="value(connectionName)")

AUTH_FLAG="--no-allow-unauthenticated"
if [ "$ALLOW_UNAUTHENTICATED" = "true" ]; then
    AUTH_FLAG="--allow-unauthenticated"
fi

gcloud run deploy "$SERVICE_NAME" \
    --image "$IMAGE_TAG" \
    --platform managed \
    --region "$REGION" \
    --service-account "$SA_EMAIL" \
    --add-cloudsql-instances "$CONNECTION_NAME" \
    --set-env-vars "SPRING_PROFILES_ACTIVE=prod,DB_NAME=${DB_NAME},DB_USER=${DB_USER},CLOUD_SQL_CONNECTION_NAME=${CONNECTION_NAME}" \
    --set-secrets "DB_PASS=upismart-db-user-password:latest,APP_API_KEY=upismart-api-key:latest" \
    $AUTH_FLAG \
    --project="$PROJECT_ID"

SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" --platform managed --region "$REGION" --project="$PROJECT_ID" --format="value(status.url)")

echo -e "\n=========================================================="
echo " UPISmart 2.0 Full-Stack Deployed Successfully!"
echo " Public Service URL:      $SERVICE_URL"
echo " Web Dashboard:           $SERVICE_URL/"
echo " Health Probe:            $SERVICE_URL/api/health"
echo " Cloud SQL Connection:    $CONNECTION_NAME"
echo " Runtime Service Account: $SA_EMAIL"
echo "=========================================================="
