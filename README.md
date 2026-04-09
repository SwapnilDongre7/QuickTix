# 🎬 QuickTix — Scalable Movie Ticket Booking System

QuickTix is a production-grade, microservices-based movie ticket booking platform built with **Spring Boot**, featuring JWT authentication, real-time seat locking, Razorpay payment integration, and a fully Dockerized deployment pipeline.

---

## 🏗 Architecture Overview

```
┌──────────┐      ┌──────────────┐       ┌───────────────────────┐
│  Client  │─────▶│  API Gateway │─────▶│  Eureka Service       │
│ (React)  │      │  (8080)      │       │  Registry (8761)      │
└──────────┘      └──────┬───────┘       └───────────────────────┘
                         │
         ┌───────────────┼───────────────────────────┐
         │               │               │           │
   ┌─────▼─────┐  ┌──────▼──────┐  ┌─────▼─────┐   ┌─▼──────────┐
   │ Identity  │  │  Catalogue  │  │  Theatre   │  │  ShowSeat  │
   │ Service   │  │  Service    │  │  Mgmt Svc  │  │  Service   │
   │ (8081)    │  │  (8087)     │  │  (8082)    │  │  (8086)    │
   └─────┬─────┘  └──────┬──────┘  └─────┬──────┘  └─┬────┬─────┘
         │               │               │           │    │
         └───────┬───────┘               │      ┌────┘    │
                 ▼                       ▼      ▼         ▼
             [ MySQL ]              [ MySQL ] [MongoDB] [Redis]
                                                          │
   ┌───────────────┐    ┌────────────────┐                │
   │ Booking Svc   │───▶│ Payment Svc    │                │
   │ (8084)        │    │ (8085)         │                │
   └───────┬───────┘    └───────┬────────┘
           │                    │
           ▼                    ▼
       [ MySQL ]           [ MySQL ]
```

---

## 🛠 Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| Language       | Java 17 / 21                            |
| Framework      | Spring Boot 3.x, Spring Cloud           |
| Security       | Spring Security + JWT                   |
| Databases      | MySQL 8.0, MongoDB 7.0                  |
| Caching/Locks  | Redis 7                                 |
| Service Disc.  | Netflix Eureka                          |
| API Gateway    | Spring Cloud Gateway                    |
| Payments       | Razorpay                                |
| Image Upload   | Cloudinary                              |
| Tracing        | Zipkin                                  |
| Build          | Maven                                   |
| Containerization | Docker & Docker Compose               |

---

## 📁 Project Structure

```
QuickTix/
├── eureka/                        # Service Registry (Eureka Server)
├── gateway/                       # API Gateway
├── identity-service/              # Auth & User Management
├── catalogue/                     # Movies, Cities, Genres, Languages
├── theatre-management-service/    # Theatres, Screens, Shows
├── showseat-service/              # Seat Layout, Availability & Locking
├── booking-service/               # Booking Lifecycle
├── payment-service/               # Razorpay Payment Processing
├── docker-compose.yml             # Full-stack Docker deployment
├── .env.example                   # Environment variable template
└── README.md
```

---

## 🧩 Microservices

### 1️⃣ Identity Service (`8081`)
- User registration & login
- JWT token generation & validation
- Role-based access control: **ADMIN**, **THEATRE_OWNER**, **USER**
- Theatre owner application & approval workflow

### 2️⃣ Catalogue Service (`8087`)
- Full CRUD for **Movies**, **Cities**, **Genres**, **Languages**
- Cloudinary-powered image/poster uploads
- Public movie browsing endpoints

### 3️⃣ Theatre Management Service (`8082`)
- Theatre registration & management by owners
- Screen configuration with seat layouts
- Show scheduling with time overlap validation
- Ownership-enforced authorization

### 4️⃣ ShowSeat Service (`8086`)
- MongoDB-based seat layout storage
- Real-time seat availability via Redis bitmask
- Redis-based temporary seat locking with TTL
- Seat lock, confirm, and unlock endpoints

### 5️⃣ Booking Service (`8084`)
- Booking creation & lifecycle management
- Integration with ShowSeat Service for seat operations
- Payment status tracking (PENDING → CONFIRMED / FAILED)
- Session-based idempotency

### 6️⃣ Payment Service (`8085`)
- Razorpay order creation & verification
- Webhook-based payment confirmation
- Automatic booking status updates

### 7️⃣ Eureka Server (`8761`)
- Service registration & discovery for all microservices

### 8️⃣ API Gateway (`8080`)
- Centralized routing to all services
- JWT authentication filter
- CORS configuration
- Zipkin tracing propagation

---

## 🔐 Security Design

- **Stateless JWT** authentication across all services
- **Role-based authorization**:
  - `ADMIN` — Platform management, owner approvals
  - `THEATRE_OWNER` — Theatre & show management
  - `USER` — Browsing, booking, payments
- **Ownership enforcement** — Theatre owners can only manage their own resources
- **Redis seat locking** — Prevents double-booking with session-based lock ownership
- **API Gateway** — Centralized auth & CORS, no per-service duplication

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (or use Docker)
- MongoDB 7.0 (or use Docker)
- Redis 7 (or use Docker)

### 1. Clone the Repository

```bash
git clone https://github.com/SwapnilDongre7/QuickTix.git
cd QuickTix
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your actual credentials
```

### 3. Run with Docker Compose (Recommended)

```bash
docker-compose up -d
```

This starts all infrastructure (MySQL, MongoDB, Redis, Zipkin) and all microservices.

### 4. Run Individually (Development)

Start infrastructure first:
```bash
docker-compose up -d mysql mongodb redis zipkin
```

Then start each service:
```bash
cd eureka && mvn spring-boot:run
cd gateway && mvn spring-boot:run
cd identity-service && mvn spring-boot:run
cd catalogue && mvn spring-boot:run
cd theatre-management-service && mvn spring-boot:run
cd showseat-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
```

---

## 🌐 API Endpoints

| Service               | Base URL                        | Swagger UI                                  |
|-----------------------|---------------------------------|---------------------------------------------|
| API Gateway           | `http://localhost:8080`         | —                                           |
| Identity Service      | `http://localhost:8081`         | `http://localhost:8081/swagger-ui.html`      |
| Theatre Management    | `http://localhost:8082`         | `http://localhost:8082/swagger-ui.html`      |
| Booking Service       | `http://localhost:8084`         | `http://localhost:8084/swagger-ui.html`      |
| Payment Service       | `http://localhost:8085`         | `http://localhost:8085/swagger-ui.html`      |
| ShowSeat Service      | `http://localhost:8086`         | `http://localhost:8086/swagger-ui.html`      |
| Catalogue Service     | `http://localhost:8087`         | `http://localhost:8087/swagger-ui.html`      |
| Eureka Dashboard      | `http://localhost:8761`         | —                                           |
| Zipkin Dashboard      | `http://localhost:9411`         | —                                           |

---

## 🧪 Running Tests

```bash
# Run tests for a specific service
cd identity-service && mvn test

# Run tests for all services
for svc in identity-service catalogue theatre-management-service showseat-service booking-service payment-service; do
  echo "Testing $svc..."
  (cd $svc && mvn test)
done
```

---

## 📝 Environment Variables

| Variable                 | Description                        | Default                                   |
|--------------------------|------------------------------------|-------------------------------------------|
| `MYSQL_ROOT_PASSWORD`    | MySQL root password                | `123456`                                  |
| `MONGO_USERNAME`         | MongoDB admin username             | `root`                                    |
| `MONGO_PASSWORD`         | MongoDB admin password             | `123456`                                  |
| `JWT_SECRET`             | JWT signing secret key             | `my-super-secure-secret-key-1234567890`   |
| `CLOUDINARY_CLOUD_NAME`  | Cloudinary cloud name              | —                                         |
| `CLOUDINARY_API_KEY`     | Cloudinary API key                 | —                                         |
| `CLOUDINARY_API_SECRET`  | Cloudinary API secret              | —                                         |
| `RAZORPAY_KEY_ID`        | Razorpay key ID                    | —                                         |
| `RAZORPAY_KEY_SECRET`    | Razorpay key secret                | —                                         |
| `RAZORPAY_WEBHOOK_SECRET`| Razorpay webhook secret            | —                                         |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## 📄 License

This project is developed for educational and portfolio purposes.

---

> Built with ❤️ by the QuickTix Team
