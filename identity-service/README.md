
Add:

```markdown
# 🔐 Identity Service

Handles authentication and authorization using JWT.

---

## Features

- User Registration
- Login
- JWT token generation
- Role-based access control
- Password encryption
- Secure ownership enforcement

---

## Roles

- ADMIN
- THEATRE_OWNER
- USER

---

## Endpoints

### Register
POST `/auth/register`

### Login
POST `/auth/login`

### Test Role
GET `/test/admin`
GET `/test/owner`
GET `/test/user`

---

## Configuration

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/identity_service
