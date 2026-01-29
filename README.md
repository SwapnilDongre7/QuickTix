# 🎬 QuickTix - Scalable Movie Ticket Booking System

QuickTix is a microservices-based movie ticket booking platform built using Spring Boot, JWT authentication, Redis, MongoDB, and MySQL.

---

## 🏗 Architecture Overview

QuickTix follows a distributed microservices architecture:

- **Identity Service** → Authentication & Authorization (JWT)
- **Show & Seat Service** → Show management, seat layout, seat locking (MongoDB + Redis)
- **Booking Service** → Booking lifecycle management (MySQL)
- **Payment Service** → Payment processing
- **API Gateway** → Routing & security (future)
- **Kafka (Planned)** → Event-driven communication

---

## 🛠 Tech Stack

- Java 17 / 21
- Spring Boot 3.x
- Spring Security + JWT
- MongoDB
- MySQL
- Redis
- Maven
- Docker (planned)
- Kafka (planned)

---

## 🔐 Security Design

- Stateless authentication using JWT
- Role-based authorization:
  - ADMIN
  - THEATRE_OWNER
  - USER
- Ownership enforcement for theatre owners
- Redis-based temporary seat locking

---

## 🧩 Services

### 1️⃣ Identity Service
Handles:
- User registration
- Login
- JWT token generation
- Role-based access control

### 2️⃣ Show & Seat Service
Handles:
- Show creation
- Pricing validation
- Time overlap validation
- Seat availability
- Redis seat locking

### 3️⃣ Booking Service (In Progress)
Handles:
- Booking creation
- Booking confirmation
- Payment status tracking

---

## 🚀 How To Run (Identity Service)

```bash
cd identity-service
mvn spring-boot:run
