# Redizego - Cab Booking System

🚖 A modern, scalable cab booking platform built with Spring Boot, designed to provide seamless ride-hailing experiences.

## 🚀 Project Overview

Redizego is a comprehensive cab booking system that connects riders with drivers in real-time. This project follows modern software architecture principles and leverages the power of Spring Boot to deliver a robust backend service.

## 🔄 Current Phase: Phase 2 - Core Functionality ✅

### ✅ Phase 1 Completed
- **Database Schema**: Complete entity relationships and data model
- **JPA Entities**:
  - User (Rider/Driver)
  - Ride
  - Payment
  - Wallet & Transactions
  - Vehicle
  - Location (with PostGIS support)
- **Design Patterns**:
  - Strategy Pattern for ride fare calculation
  - Strategy Pattern for driver matching
- **Project Structure**:
  - Modular package organization
  - Clear separation of concerns

### ✅ Phase 2 Completed
- **Complete REST API Implementation**:
  - `AuthController`: User authentication, signup, login, driver onboarding
  - `RiderController`: Ride requests, cancellation, profile management, rating
  - `DriverController`: Ride acceptance, start/end rides, profile management, rating
  - Full CRUD operations with proper error handling

- **Spring Security with JWT Authentication**:
  - Role-based access control (RIDER, DRIVER, ADMIN)
  - JWT token generation and validation
  - Refresh token mechanism with HTTP-only cookies
  - Method-level security annotations

- **API Documentation**:
  - Swagger UI integration with OpenAPI 3.0
  - Interactive API documentation available at `/swagger-ui.html`
  - Comprehensive endpoint documentation with request/response examples

- **Email Service Integration**:
  - Spring Boot Mail implementation
  - Service-layer email functionality (tested and working)
  - Ready for OTP verification and notifications

- **Database Optimizations**:
  - Schema-level performance improvements
  - Proper indexing for geospatial queries
  - Optimized entity relationships

## 🔧 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.0
- **Security**: 
  - Spring Security
  - JWT (JSON Web Tokens)
- **Persistence**: 
  - Spring Data JPA
  - PostgreSQL with PostGIS
- **Documentation**: 
  - Swagger/OpenAPI 3.0
  - SpringDoc OpenAPI
- **Messaging**: 
  - Spring Boot Mail
- **Build Tool**: Maven
- **Utilities**: 
  - Lombok
  - ModelMapper

## 📁 Project Structure

```
src/main/java/com/redizego/redi_ze_go/
├── advices/           # Global exception handlers
├── configs/           # Configuration classes
├── controllers/       # REST controllers
├── dtos/              # Data Transfer Objects
├── entities/          # JPA entities
│   └── enums/        # Enumerations
├── exceptions/        # Custom exceptions
├── repositories/      # JPA repositories
├── security/          # JWT and security components
├── services/          # Service interfaces
│   └── impl/         # Service implementations
├── strategies/        # Strategy pattern implementations
│   └── impl/
└── utils/            # Utility classes
```

## 🎯 Next Milestones

### Phase 3: Testing & Deployment
- [ ] Unit testing all endpoints using Mockito
- [ ] Integration testing of the application
- [ ] Deploying over AWS with CI/CD enabled
- [ ] Load testing and performance optimization

### Phase 4: Advanced Features
- [ ] Real-time location tracking with WebSocket
- [ ] Offline booking capabilities
- [ ] Weather-based fare calculation

## 🚀 How to Run

### Prerequisites
- Java 21
- Maven
- PostgreSQL with PostGIS extension

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <https://github.com/Ayush0x/redizego-backend.git>
   cd redi-ze-go
   ```

2. **Configure Database**
   - Create a PostgreSQL database
   - Update `application.properties` with your database credentials

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API**
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html`
   - **API Documentation**: `http://localhost:8080/v3/api-docs`
   - **Application Health**: `http://localhost:8080/actuator/health`

## 👥 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

---

*Redizego - Making urban mobility seamless and efficient*
