# Kitchen Admin Application

A Spring Boot application for restaurant kitchen administration.

## Building and Running with Docker

### Prerequisites

- Docker

### Building the Docker Image

The Dockerfile uses a multi-stage build process that automatically builds the application, so you don't need to build it locally first.

1. Build the Docker image:

```bash
docker build -t kitchen-admin .
```

2. Run the Docker image locally:

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/kitchen \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PWD=postgres \
  -e ADMIN_NAME=admin \
  -e ADMIN_PWD=admin \
  -e NOTIFICATION_TOKEN=your_telegram_token \
  kitchen-admin
```

## Deploying to Digital Ocean App Platform

1. Push your code to a Git repository (GitHub, GitLab, etc.)

2. Log in to your Digital Ocean account and navigate to the App Platform

3. Click "Create App" and select your Git repository

4. Configure the app:
   - Select the Dockerfile as the build method
   - Set the HTTP port to 8080
   - Configure environment variables:
     - `DATABASE_URL`: Your PostgreSQL database URL
     - `DATABASE_USERNAME`: Database username
     - `DATABASE_PWD`: Database password
     - `ADMIN_NAME`: Admin username for the application
     - `ADMIN_PWD`: Admin password for the application
     - `NOTIFICATION_TOKEN`: Telegram bot token

5. Configure resources:
   - Select an appropriate plan for your needs
   - For production, consider at least 1GB RAM

6. Click "Launch App" to deploy

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DATABASE_URL | PostgreSQL database URL | jdbc:postgresql://localhost:5432/kitchen |
| DATABASE_USERNAME | Database username | postgres |
| DATABASE_PWD | Database password | postgres |
| ADMIN_NAME | Admin username | admin |
| ADMIN_PWD | Admin password | admin |
| NOTIFICATION_TOKEN | Telegram bot token | adminadminadmin |

## Features

- Restaurant visitor management
- Booking management
- Staff notification via Telegram
- Admin panel for booking management
- Public booking form for customers
