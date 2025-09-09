## Project Overview

This repository contains the **backend part** of the Collector Admin demo system.  
The project is built with **Spring Boot 2.7**, **JPA**, and **PostgreSQL**, and is designed to work in a **front-end / back-end separated architecture**.  
The corresponding frontend implementation can be found here:  
👉 [collector-admin-web](https://github.com/dan9574/collector-admin-web)

## Database Integration

- Backend is connected to a **PostgreSQL** database.  
- Entities are managed using **Spring Data JPA**.  
- Database connection details (URL, username, password) are configured via environment variables in `application.yml`.  
- Example values used in this demo:
  - `DB_URL=jdbc:postgresql://localhost:5432/collector`
  - `DB_USER=app`
  - `DB_PASS=app`

> ⚠️ All database usernames, passwords, and connection strings shown here are **demo values only**.  
> They are used solely for local testing and reproducibility, and do **not** represent any real or sensitive credentials.  
> No real company data is included in this repository.

## Usage

1. Start a local PostgreSQL instance (example with Docker):

         docker run --name collector-db \
           -e POSTGRES_USER=app \
           -e POSTGRES_PASSWORD=app \
           -e POSTGRES_DB=collector \
           -p 5432:5432 \
           -d postgres:15
   
2. Run the backend server (default port: 8081):
```bash
  ./mvnw spring-boot:run
```

Start the frontend (collector-admin-web) and access the system UI in your browser.

This backend repo and the linked frontend repo together form a complete full-stack demo showcasing authentication, task management, templates, and export modules in a separated architecture.
