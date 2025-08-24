# FinMate Backend

A Spring Boot 3 backend application for the FinMate personal finance management system.

## Features

- **User Authentication**: JWT-based authentication with registration and login
- **User Management**: User CRUD operations with role-based access control
- **Invitation System**: User invitation management for team collaboration
- **Database Migration**: Automatic schema management with Flyway
- **API Documentation**: Swagger UI integration for API exploration
- **Security**: Spring Security with BCrypt password encoding

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6**
- **Spring Data JPA**
- **MySQL Database**
- **Flyway Migration**
- **JWT Authentication**
- **OpenAPI 3 (Swagger)**
- **Maven**
- **Lombok**

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Environment Variables

Set the following environment variables or use the default values:

```bash
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/finmate?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=password

# JWT Configuration
JWT_SECRET=your-secret-key-here-change-in-production
JWT_EXPIRATION=86400000
```

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd finmate-backend
   ```

2. **Set up MySQL database**
   - Ensure MySQL is running
   - Create a database named `finmate` (or it will be created automatically)
   - Update environment variables if needed

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - Application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/me` - Get current user info

### Request Examples

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

#### Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

#### Get Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Database Schema

The application automatically creates the following tables via Flyway migrations:

### Users Table
- `id` - Primary key
- `name` - User's full name
- `email` - Unique email address
- `password_hash` - BCrypt hashed password
- `role` - User role (ADMIN, MEMBER)
- `created_at` - Account creation timestamp
- `updated_at` - Last update timestamp

### Invitations Table
- `id` - Primary key
- `email` - Invited user's email
- `role` - Invited role
- `status` - Invitation status (PENDING, ACCEPTED, DECLINED, EXPIRED)
- `token` - Unique invitation token
- `invited_at` - Invitation timestamp
- `expires_at` - Expiration timestamp
- `invited_by` - Reference to inviting user

## Security Features

- **JWT Authentication**: Stateless authentication with configurable expiration
- **Password Security**: BCrypt password hashing
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Role-based Access**: User roles for authorization
- **Input Validation**: Bean validation for all inputs

## Development

### Project Structure
```
src/main/java/com/finmate/
├── config/          # Configuration classes
├── auth/            # Authentication and authorization
├── users/           # User management
├── invitations/     # Invitation system
├── common/          # Shared utilities and base classes
└── FinmateBackendApplication.java
```

### Adding New Features
1. Create entity classes extending `BaseEntity`
2. Create repository interfaces extending `JpaRepository`
3. Create service classes with business logic
4. Create DTOs for data transfer
5. Create controllers for API endpoints
6. Add appropriate security configurations

## Testing

Run tests with:
```bash
mvn test
```

The project includes test configuration that uses H2 in-memory database and disables security for testing purposes.

## Building for Production

```bash
mvn clean package -DskipTests
```

The JAR file will be created in the `target/` directory.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
