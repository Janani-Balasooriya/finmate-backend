# FinMate – Backend

**FinMate** is a personal and family finance management app that helps households track income and expenses, plan budgets, manage subscriptions and work toward shared savings goals.

This repository contains the **Spring Boot 3** REST API. The React web client lives in **[finmate-frontend](https://github.com/Janani-Balasooriya/finmate-frontend)**.

**FinMate dashboard**
<img width="1920" height="2330" alt="image" src="https://github.com/user-attachments/assets/60de2455-2c14-4e44-853b-69f7edc254fe" />

**Budgets** 
<img width="1920" height="1552" alt="image" src="https://github.com/user-attachments/assets/72bdd584-4392-4adf-aa50-2084bfab07cc" />

**Subscriptions**
<img width="1920" height="970" alt="image" src="https://github.com/user-attachments/assets/cf9faa06-6ab3-4680-81de-b6e325221998" />


## Highlights

- **Layered architecture:** Controller → Service → Repository, with DTOs between the API and the domain.
- **JWT authentication:** stateless auth with configurable expiry; BCrypt password hashing.
- **Role-based access control:** Admin and Member roles, with family-member authorization checks in both the service and controller layers, so users can only reach their own household's data.
- **Family invitations:** token-based invites with status tracking (Pending, Accepted, Declined, Expired) and expiry.
- **Exchange rates:** scheduled job that keeps currency exchange rates up to date.
- **Database migrations:** schema managed with Flyway on MySQL.
- **API documentation:** OpenAPI 3 / Swagger UI, plus a [Swagger testing guide](SWAGGER_TESTING_GUIDE.md).
- **Validation and security:** Bean Validation on all inputs, configurable CORS.
- **Testing:** test profile on an H2 in-memory database.

## Tech stack

Java 17 · Spring Boot 3.2 · Spring Security 6 · Spring Data JPA / Hibernate · MySQL 8 · Flyway · JWT · OpenAPI 3 (Swagger) · Maven · Lombok · H2 (tests)

## Getting started

### Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.6+ (or use the included `mvnw` wrapper)

### Configuration

Set these environment variables (local defaults shown; always change secrets outside local development):

```bash
DB_URL=jdbc:mysql://localhost:3306/finmate?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your-password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Run

```bash
git clone https://github.com/Janani-Balasooriya/finmate-backend.git
cd finmate-backend
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/api-docs`

### Test

```bash
./mvnw test
```

### Build

```bash
./mvnw clean package
```

The JAR is created in `target/`.

## API overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Log in and receive a JWT |
| GET | `/api/auth/me` | Current user's profile |

The full list of endpoints, with request and response schemas, is in Swagger UI.

**Example: log in**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "password123"}'
```

## Project structure

```
src/main/java/com/finmate/
├── config/          # Security, CORS, OpenAPI configuration
├── auth/            # Authentication and JWT
├── users/           # User management
├── invitations/     # Family invitation system
├── common/          # Shared base classes and utilities
└── FinmateBackendApplication.java
```

## Author

**Janani Balasooriya**: designed and built FinMate end to end (backend and frontend).
[GitHub](https://github.com/Janani-Balasooriya) · [LinkedIn](https://linkedin.com/in/janani-balasooriya)

## License

MIT
