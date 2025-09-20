# 🔍 FinMate Backend - Swagger Testing Guide

## 📋 Overview
This guide explains how to use Swagger UI to test the FinMate backend API endpoints.

## 🚀 Accessing Swagger UI

### **URLs:**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/api-docs`

## 🔧 How to Test Authentication

### **Step 1: Start the Application**
```bash
cd finmate-backend
mvn spring-boot:run
```

### **Step 2: Open Swagger UI**
Navigate to `http://localhost:8080/swagger-ui.html` in your browser.

### **Step 3: Test Registration**
1. Find the **Authentication** section
2. Click on `POST /api/auth/register`
3. Click **"Try it out"**
4. Use this sample payload:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```
5. Click **"Execute"**
6. Copy the `accessToken` from the response

### **Step 4: Test Login**
1. Click on `POST /api/auth/login`
2. Click **"Try it out"**
3. Use this sample payload:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
4. Click **"Execute"**
5. Copy the `accessToken` from the response

### **Step 5: Test Protected Endpoints**
1. Click the **"Authorize"** button at the top right
2. Enter: `Bearer <your-jwt-token>`
3. Click **"Authorize"**
4. Now test protected endpoints:
   - `GET /api/auth/me` - Get current user info
   - `GET /api/test/protected` - Test protected endpoint
   - `POST /api/auth/logout` - Logout

## 🧪 Test Endpoints

### **Public Endpoints (No Authentication Required):**
- `GET /api/test/public` - Test public endpoint
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### **Protected Endpoints (JWT Token Required):**
- `GET /api/auth/me` - Get current user information
- `POST /api/auth/logout` - User logout
- `GET /api/test/protected` - Test protected endpoint

## 🔐 JWT Token Testing

### **Token Format:**
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **Token Information:**
- **Algorithm**: HS256
- **Expiration**: 24 hours (86400000 ms)
- **Contains**: User email and role information

## 📊 Response Examples

### **Successful Login Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "MEMBER",
      "status": "ACTIVE"
    }
  }
}
```

### **Error Response:**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null
}
```

## 🛠️ Troubleshooting

### **Common Issues:**

1. **401 Unauthorized**
   - Check if JWT token is valid
   - Ensure token is prefixed with "Bearer "
   - Verify token hasn't expired

2. **400 Bad Request**
   - Check request payload format
   - Verify required fields are provided
   - Check validation constraints

3. **CORS Issues**
   - CORS is configured to allow all origins
   - Check if frontend URL is correct

### **Database Requirements:**
- Ensure MySQL is running on `localhost:3307`
- Database name: `finmate_db`
- Username: `root`
- Password: `1234`

## 📝 API Documentation Features

### **Swagger UI Features:**
- **Interactive Testing**: Test endpoints directly from the browser
- **Request/Response Examples**: See real examples for each endpoint
- **Authentication**: Built-in JWT token testing
- **Schema Validation**: Automatic request validation
- **Error Handling**: See different error responses

### **Documentation Includes:**
- Endpoint descriptions and summaries
- Request/response schemas
- Authentication requirements
- Example payloads
- Error codes and messages
- Parameter validation rules

## 🎯 Testing Workflow

1. **Start Application** → `mvn spring-boot:run`
2. **Open Swagger** → `http://localhost:8080/swagger-ui.html`
3. **Register User** → Use `/api/auth/register`
4. **Login User** → Use `/api/auth/login`
5. **Authorize** → Add JWT token to Swagger
6. **Test Protected Endpoints** → Use authenticated endpoints
7. **Verify Responses** → Check data and error handling

## 🔍 Additional Features

### **Swagger Configuration:**
- **API Title**: FinMate Backend API
- **Version**: 1.0.0
- **Security**: JWT Bearer Authentication
- **CORS**: Enabled for all origins
- **Sorting**: Operations sorted by method, tags alphabetically

### **Customization:**
- API documentation is auto-generated from annotations
- Easy to add new endpoints with proper documentation
- Consistent response format across all endpoints
- Professional API documentation for team collaboration

---

**Happy Testing! 🚀**
