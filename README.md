# Redizego - Cab Booking System

🚖 A modern, scalable cab booking platform built with Spring Boot, designed to provide seamless ride-hailing experiences.

## 🚀 Project Overview

Redizego is a comprehensive cab booking system that connects riders with drivers in real-time. This project follows modern software architecture principles and leverages the power of Spring Boot to deliver a robust backend service.

## 🔄 Current Phase: Prototype 1

### ✅ Completed
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

### 🔄 In Progress
- Service layer implementation
- Business logic development
- API endpoints

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.3
- **Persistence**: 
  - Spring Data JPA
  - PostgreSQL with PostGIS
- **Build Tool**: Maven
- **Lombok**: For boilerplate reduction

## 📁 Project Structure

```
src/main/java/com/redizego/redi_ze_go/
├── configs/           # Configuration classes
├── controllers/       # REST controllers (future)
├── dtos/              # Data Transfer Objects
├── entities/          # JPA entities
│   └── enums/        # Enumerations
├── exceptions/        # Custom exceptions
├── repositories/      # JPA repositories
├── services/          # Service interfaces
│   └── impl/         # Service implementations
└── strategies/       # Strategy pattern implementations
    └── impl/
```

## 🎯 Next Milestones

### Phase 2: Core Functionality
- [ ] Implement service layer
- [ ] Add authentication & authorization
- [ ] Implement real-time location tracking
- [ ] Set up WebSocket for real-time updates

### Phase 3: API & Integration
- [ ] REST API endpoints
- [ ] Integration with payment gateways
- [ ] Notification service
- [ ] Rate limiting and API documentation

### Phase 4: Advanced Features
- [ ] Ride scheduling
- [ ] Multi-stop rides
- [ ] Surge pricing
- [ ] Advanced analytics

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

## 👥 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

---

*Redizego - Making urban mobility seamless and efficient*
