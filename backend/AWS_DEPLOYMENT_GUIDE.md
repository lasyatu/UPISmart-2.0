# UPISmart 2.0 — AWS Cloud Console Deployment Guide

This guide provides complete, step-by-step instructions to deploy the **UPISmart 2.0 Backend** and **MySQL Relational Database** using the **AWS Management Console (Web UI)**.

---

## Architecture Overview on AWS

```
  [ Android Client App / Web Browser ]
                  │
                  ▼ (HTTPS)
   ┌──────────────────────────────┐
   │        AWS App Runner        │  <-- Fully-managed container service
   │  (Spring Boot Java 17 Engine)│      (Direct AWS equivalent of Cloud Run)
   │         Port: 8080           │      Automatic SSL, Zero server maintenance
   └──────────────┬───────────────┘
                  │ (VPC Connector)
                  ▼
   ┌──────────────────────────────┐
   │     Amazon RDS for MySQL     │  <-- Managed Relational Database
   │     (MySQL 8.0 / db.t4g)     │      Persistent storage for transactions,
   │       Database: upismart_db  │      emergency vault, users & audit logs
   └──────────────────────────────┘
                  ▲
                  │
   ┌──────────────────────────────┐
   │     AWS Secrets Manager      │  <-- Encrypted storage for DB passwords
   │    (upismart/db/credentials) │      and application API security keys
   └──────────────────────────────┘
```

---

## Method 1: AWS Management Console (Point-and-Click Web UI)

### Step 1: Create Amazon RDS MySQL Database

1. Sign in to the **[AWS Management Console](https://console.aws.amazon.com/)**.
2. In the top search bar, type **RDS** and click **Databases**.
3. Click the orange **Create database** button.
4. Choose database creation method:
   - Select **Standard create**.
5. Engine options:
   - Engine type: **MySQL**
   - Engine Version: **MySQL 8.0.35** (or latest 8.0.x)
6. Templates:
   - Select **Free tier** (uses `db.t4g.micro` or `db.t3.micro`, 1 vCPU, 1 GB RAM).
7. Settings:
   - **DB instance identifier**: `upismart-mysql-db`
   - **Master username**: `upismart_user`
   - **Master password**: Enter a strong password (or click *Auto generate a password*). **Save this password securely.**
8. Instance configuration:
   - Burstable classes: `db.t4g.micro` (or `db.t3.micro`).
9. Storage:
   - Storage type: **General Purpose SSD (gp3)**
   - Allocated storage: **20 GiB** (Free tier eligible)
   - Enable storage autoscaling: **Unchecked** (to prevent accidental costs).
10. Connectivity:
    - Compute resource: **Don't connect to an EC2 compute resource** (we will connect from App Runner).
    - Virtual private cloud (VPC): **Default VPC**
    - Public access: **Yes** (or use VPC Connector if private).
    - VPC security group: Select **Create new**, name it `upismart-rds-sg`.
11. Additional configuration:
    - Initial database name: `upismart_db`
    - Backup retention: **1 day**
12. Click **Create database**.
    - *Note: RDS takes approximately 4–7 minutes to initialize.*
    - Once the status shows **Available**, click on `upismart-mysql-db` and copy the **Endpoint** (e.g., `upismart-mysql-db.c123456.us-east-1.rds.amazonaws.com`).

---

### Step 2: Store Credentials in AWS Secrets Manager

1. In the console search bar, type **Secrets Manager** and click on the service.
2. Click **Store a new secret**.
3. Secret type:
   - Choose **Credentials for Amazon RDS database**.
   - User name: `upismart_user`
   - Password: Paste the password created in Step 1.
   - Database: Select `upismart-mysql-db`.
4. Secret name:
   - Name: `upismart/db/credentials`
   - Description: `Production database credentials for UPISmart 2.0`
5. Click **Next** through automatic rotation (disable for now), and click **Store**.

---

### Step 3: Create Amazon ECR Repository & Push Docker Image

AWS App Runner runs container images from **Amazon Elastic Container Registry (ECR)**:

1. In the console search bar, search for **ECR** and click **Repositories**.
2. Click **Create repository**.
   - Visibility settings: **Private**
   - Repository name: `upismart-backend`
   - Click **Create repository**.
3. Click into `upismart-backend` and click the **View push commands** button.
4. Open **AWS CloudShell** (the terminal icon in the top header bar of AWS Console) or your local terminal, and run:
   ```bash
   # 1. Log in to your ECR registry
   aws ecr get-login-password --region <YOUR_AWS_REGION> | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.<YOUR_AWS_REGION>.amazonaws.com

   # 2. Build the Docker image from backend directory
   cd backend
   docker build -t upismart-backend .

   # 3. Tag and Push to ECR
   docker tag upismart-backend:latest <ACCOUNT_ID>.dkr.ecr.<YOUR_AWS_REGION>.amazonaws.com/upismart-backend:latest
   docker push <ACCOUNT_ID>.dkr.ecr.<YOUR_AWS_REGION>.amazonaws.com/upismart-backend:latest
   ```

---

### Step 4: Deploy Service on AWS App Runner

1. In the AWS Console search bar, search for **App Runner**.
2. Click **Create service**.
3. **Step 1: Source and deployment**:
   - Repository type: **Container registry**
   - Provider: **Amazon ECR**
   - Container image URI: Click **Browse** and select `upismart-backend:latest`.
   - Deployment trigger: Choose **Manual** or **Automatic**.
   - ECR access role: Choose **Create new service role** (App Runner automatically configures the IAM permissions to pull from your ECR).
   - Click **Next**.
4. **Step 2: Configure service**:
   - Service name: `upismart-backend`
   - Virtual CPU & Memory: **1 vCPU, 2 GB**
   - **Port**: Change default `80` to **`8080`** (Spring Boot listens on 8080).
5. **Environment variables**:
   Add the following key-value pairs:
   | Key | Value | Description |
   |---|---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` | Activates production configuration |
   | `DB_HOST` | `<YOUR_RDS_ENDPOINT>` | The RDS Endpoint from Step 1 |
   | `DB_PORT` | `3306` | Default MySQL port |
   | `DB_NAME` | `upismart_db` | Initial database name |
   | `DB_USER` | `upismart_user` | Database username |
   | `DB_PASS` | `<YOUR_RDS_PASSWORD>` | Master database password |
   | `APP_API_KEY` | `upismart-secure-key-2026` | API authentication key |
6. **Health check**:
   - Protocol: **HTTP**
   - Path: `/api/health`
   - Interval: `10` seconds, Timeout: `5` seconds, Healthy threshold: `1`
7. Click **Next** -> Review all settings -> Click **Create & deploy**.

---

### Step 5: Verify Deployment & Access Live Endpoints

1. App Runner will take 2–3 minutes to provision the container and pass health checks.
2. Once the status shows **Running**, copy the **Default domain** URL (e.g., `https://abcdefghij.us-east-1.awsapprunner.com`).
3. Test the endpoints in your browser:
   - **System Health**: `https://<YOUR_APP_RUNNER_URL>/api/health`
     ```json
     {
       "status": "UP",
       "service": "UPISmart 2.0 Backend",
       "timestamp": "2026-10-16T..."
     }
     ```
   - **Live Web Dashboard**: `https://<YOUR_APP_RUNNER_URL>/`
   - **Transactions API**: `https://<YOUR_APP_RUNNER_URL>/api/transactions`
   - **Emergency Vault API**: `https://<YOUR_APP_RUNNER_URL>/api/vault`

---

## Method 2: 1-Click AWS CloudShell Automated Deployment

If you prefer to deploy automatically using the AWS CloudShell:

1. Click the **CloudShell icon** `>_` in the top navigation bar of the AWS Console.
2. Upload or clone the project repository.
3. Run the automated script:
   ```bash
   chmod +x backend/deploy-aws.sh
   ./backend/deploy-aws.sh
   ```
4. The script automatically creates:
   - AWS Secrets Manager credentials
   - Amazon RDS MySQL database
   - Amazon ECR repository and builds the container
   - AWS App Runner service with live HTTPS URL

---

## Connecting the Android Mobile App to AWS

In the Android application, set the backend base URL to your AWS App Runner service:

In your Android project's `app/src/main/java/com/upismart/android/data/network/` or `.env`:
```properties
BACKEND_BASE_URL=https://<YOUR_APP_RUNNER_URL>/
```
All UPI transactions, Safe-to-Spend calculations, Salary Utilization meters, and Emergency Vault reserves will synchronize in real time with your AWS cloud backend!
