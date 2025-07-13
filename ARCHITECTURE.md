# Architecture Documentation

## Overview
This document provides an architectural overview of the Redizego backend system. The project is built with Spring Boot and follows modern microservice architecture patterns.

## Components

### 1. **Controllers**
- **Responsibility**: Handle client requests, perform authorization/authentication, and delegate business logic to services.
- **Key Files**:
  - `AuthController.java`: Manages authentication and user management operations
  - `DriverController.java`: Handles operations specific to drivers
  - `RiderController.java`: Handles operations specific to riders

### 2. **Services**
- **Responsibility**: Implement business logic, coordinate interactions between repositories and controllers.
- **Key Implementations**:
  - `AuthService`: Handles authentication-related business logic
  - `DriverService`: Business logic related to drivers, ride acceptance, etc.
  - `RiderService`: Business logic related to riders, ride requests, etc.

### 3. **Repositories**
- **Responsibility**: Provide CRUD operations for entities and abstract persistence details.
- **Technology**: Spring Data JPA

### 4. **Entities**
- **Responsibility**: Represent the domain model, mapped to database tables via JPA annotations.
- **Key Entities**:
  - `User.java`, `Driver.java`, `Rider.java`, `Ride.java`, etc.

### 5. **DTOs**
- **Responsibility**: Transfer data between client and server, preventing direct exposure of entities.
- **Key DTOs**:
  - `SignupDto`, `LoginRequestDto`, `RideRequestDto`, etc.

### 6. **Security**
- **Responsibility**: Implement authentication/authorization via JWT, role-based access.
- **Key Implementations**:
  - `JWTService`: Manages JWT token generation and validation
  - `WebSecurityConfig`: Configures security settings

## Design Patterns

### 1. **Strategy Pattern**
- **Usage**: Implements interchangeable family of algorithms for driver matching, ride fare calculation, etc.
- **Key Strategies**:
  - `DriverMatchingStrategy`: Strategy interface for driver matching
    - `DriverMatchingNearestDriverStrategy`: Implementation matching nearest available drivers
  - `RideFareCalculationStrategy`: Strategy interface for fare calculation
    - `RideFareDefaultFareCalculationStrategy`: Default fare calculation based on distance
  - `PaymentStrategy`: Strategy interface for payment processing
    - `WalletPaymentStrategy`: Implements payment process using wallet balance 

## Technologies Used

- **Spring Boot**: For application framework
- **Spring Data JPA**: For database interactions
- **PostgreSQL with PostGIS**: For geospatial data
- **JWT (JSON Web Token)**: For secure authentication

## Future Enhancements

1. **Microservices Transition**
   - Break down the monolithic application into microservices for scalability

2. **Load Balancing & Scaling**
   - Implement load balancing to manage increased user traffic
   - Horizontal scaling of services

3. **Real-time Features**
   - Incorporate WebSocket connections for live tracking and updates

## Conclusion
The Redizego backend employs a modular, scalable architecture suitable for modern cloud deployment. The combination of patterns, technology choices, and structured layers ensures maintainability and extensibility.
