# AWS Deployment Guide for Final LMS

## Overview
This project is a Spring Boot application with an embedded frontend. You have two main options for deployment:

1.  **Unified Deployment (Recommended):** Run both Backend and Frontend on AWS. Use GoDaddy only for the Domain Name.
2.  **Separated Deployment:** Run Backend on AWS and Frontend on GoDaddy Hosting.

---

## Prerequisites
-   AWS Account (for Backend)
-   GoDaddy Account (for Domain/Frontend)
-   TiDB Cloud Account (for Database)
-   Docker (optional, but recommended)

---

## Step 1: Database Setup (TiDB)
Ensure your TiDB cluster is running. You need the following details:
-   `DB_URL` (e.g., `jdbc:mysql://gateway01.us-west-2.prod.aws.tidbcloud.com:4000/final_lms?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3`)
-   `DB_USER`
-   `DB_PASS`

---

## Option 1: Unified Deployment (Easiest)
Deploy the entire application to AWS. The frontend will be served by the Java application.

### A. Deploy to AWS App Runner (Serverless)
1.  **Push to GitHub:** Ensure your code is in your GitHub repository.
2.  **Go to AWS App Runner Console:** Create a new service.
3.  **Source:** Select "Source Code Repository" and link your GitHub repo.
4.  **Configure Build:**
    -   **Runtime:** Java Corretto 17
    -   **Build Command:** `mvn -f backend/pom.xml clean package -DskipTests`
    -   **Start Command:** `java -jar backend/target/backend-0.0.1-SNAPSHOT.jar`
    -   **Port:** `8092`
5.  **Environment Variables:** Add the variables from `render.yaml` (DB_URL, DB_USER, DB_PASS, AWS keys, etc.).
6.  **Deploy:** Wait for it to finish. You will get a default URL (e.g., `https://xyz.awsapprunner.com`).
7.  **GoDaddy:** Go to DNS Management and add a CNAME record pointing `www` to your App Runner URL.

### B. Deploy to AWS EC2 (Virtual Machine)
1.  Launch an EC2 instance (Ubuntu or Amazon Linux 2023).
2.  Install Java 17 and Docker.
3.  Copy the JAR file or Clone the repo and build it on the server.
4.  Run the JAR:
    ```bash
    export DB_URL="jdbc:mysql://..."
    export DB_USER="root"
    # ... export other vars ...
    java -jar backend-0.0.1-SNAPSHOT.jar
    ```
5.  Configure Security Group to allow traffic on port 8092 (or 80 using Nginx).

---

## Option 2: Separated Deployment (Frontend on GoDaddy)
Use this if you want to host static files (HTML/CSS/JS) on GoDaddy's cPanel/Hosting.

### 1. Prepare Backend (AWS)
Follow the steps in Option 1 (App Runner or EC2) to deploy the backend.
**Note the Backend URL** (e.g., `https://api.myapp.com`).

### 2. Prepare Frontend
1.  Locate the frontend files in `backend/src/main/resources/static`.
2.  **Update API URL:**
    -   Open `js/shared.js`.
    -   Change `const API_BASE = '/api';` to your AWS Backend URL:
        ```javascript
        const API_BASE = 'https://your-aws-backend-url.com/api';
        ```
3.  **Zip the contents** of the `static` folder (not the folder itself, but the contents: `admin`, `student`, `index.html`, etc.).

### 3. Upload to GoDaddy
1.  Login to GoDaddy File Manager (cPanel).
2.  Upload the Zip file to `public_html`.
3.  Extract the Zip file.

### 4. Configure CORS on Backend
If you face CORS errors, you might need to update `CorsConfig.java` to explicitly allow your GoDaddy domain, though currently it allows `*` (All), so it should work out of the box.

---

## Environment Variables Reference
Set these in your AWS Configuration:

| Variable | Description |
| :--- | :--- |
| `DB_URL` | TiDB JDBC URL |
| `DB_USER` | Database User |
| `DB_PASS` | Database Password |
| `JWT_SECRET` | Secret for Tokens |
| `AWS_ACCESS_KEY` | For S3 |
| `AWS_SECRET_KEY` | For S3 |
| `AWS_BUCKET` | S3 Bucket Name |
| `AWS_REGION` | AWS Region |
