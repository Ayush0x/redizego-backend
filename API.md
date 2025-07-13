# Redizego API Documentation

## Overview
This document provides comprehensive API documentation for the Redizego cab booking system backend. The API follows RESTful principles and uses JWT for authentication.

## Base URL
```
http://localhost:8080
```

## Authentication
The API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:
```
Authorization: Bearer <token>
```

## Global Response Format
All API responses follow a consistent structure handled by `GlobalResponseHandler`:

### Success Response
```json
{
  "data": <response_data>,
  "error": null,
  "timeStamp": "2024-01-01T10:00:00Z"
}
```

### Error Response
```json
{
  "data": null,
  "error": {
    "message": "Error description",
    "subErrors": ["detailed error 1", "detailed error 2"]
  },
  "timeStamp": "2024-01-01T10:00:00Z"
}
```

## API Endpoints

### Authentication Endpoints

#### 1. User Signup
- **POST** `/auth/signup`
- **Description**: Register a new user
- **Authentication**: None required
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response**: `UserDto` object
- **Status**: 201 Created

#### 2. User Login
- **POST** `/auth/login`
- **Description**: Authenticate user and get access token
- **Authentication**: None required
- **Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Additional**: Sets httpOnly refresh token cookie
- **Status**: 200 OK

#### 3. Refresh Token
- **POST** `/auth/refresh-token`
- **Description**: Get new access token using refresh token
- **Authentication**: Refresh token in cookie
- **Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Status**: 200 OK

#### 4. Driver Onboarding
- **POST** `/auth/onboard-driver/{user-id}`
- **Description**: Convert a user to a driver
- **Authentication**: Required (ROLE_ADMIN)
- **Path Parameters**:
  - `user-id`: User ID to convert to driver
- **Request Body**:
```json
{
  "vehicleId": "ABC123"
}
```
- **Response**: `DriverDto` object
- **Status**: 201 Created

---

### Rider Endpoints
**Base Path**: `/rider`  
**Authentication**: Required (ROLE_RIDER)

#### 1. Request Ride
- **POST** `/rider/request-ride`
- **Description**: Request a new ride
- **Request Body**:
```json
{
  "pickupLocation": {
    "latitude": 28.7041,
    "longitude": 77.1025
  },
  "destinationLocation": {
    "latitude": 28.5355,
    "longitude": 77.3910
  },
  "paymentMethod": "WALLET"
}
```
- **Response**: `RideRequestDto` object
- **Status**: 200 OK

#### 2. Cancel Ride
- **POST** `/rider/cancel-ride/{ride-id}`
- **Description**: Cancel an ongoing ride
- **Path Parameters**:
  - `ride-id`: ID of the ride to cancel
- **Response**: `RideDto` object
- **Status**: 200 OK

#### 3. Get Rider Profile
- **GET** `/rider/my-profile`
- **Description**: Get current rider's profile
- **Response**: `RiderDto` object
- **Status**: 200 OK

#### 4. Get All Rides
- **GET** `/rider/all-rides`
- **Description**: Get paginated list of rider's rides
- **Query Parameters**:
  - `pageOffset` (optional): Page number (default: 0)
  - `pageSize` (optional): Page size (default: 10)
- **Response**: `Page<RideDto>` object
- **Status**: 200 OK

#### 5. Rate Driver
- **POST** `/rider/rate-driver/{ride-id}/{rating}`
- **Description**: Rate a driver after ride completion
- **Path Parameters**:
  - `ride-id`: ID of the completed ride
  - `rating`: Rating value (1-5)
- **Response**: `DriverDto` object
- **Status**: 200 OK

---

### Driver Endpoints
**Base Path**: `/driver`  
**Authentication**: Required (ROLE_DRIVER)

#### 1. Accept Ride
- **POST** `/driver/accept-ride/{ride-request-id}`
- **Description**: Accept a ride request
- **Path Parameters**:
  - `ride-request-id`: ID of the ride request to accept
- **Response**: `RideDto` object
- **Status**: 200 OK

#### 2. Start Ride
- **POST** `/driver/start-ride/{ride-request-id}`
- **Description**: Start an accepted ride using OTP
- **Path Parameters**:
  - `ride-request-id`: ID of the ride request
- **Request Body**:
```json
{
  "otp": "1234"
}
```
- **Response**: `RideDto` object
- **Status**: 200 OK

#### 3. End Ride
- **POST** `/driver/end-ride/{ride-id}`
- **Description**: End an ongoing ride
- **Path Parameters**:
  - `ride-id`: ID of the ride to end
- **Response**: `RideDto` object
- **Status**: 200 OK

#### 4. Cancel Ride
- **POST** `/driver/cancel-ride/{ride-id}`
- **Description**: Cancel a ride
- **Path Parameters**:
  - `ride-id`: ID of the ride to cancel
- **Response**: `RideDto` object
- **Status**: 200 OK

#### 5. Get Driver Profile
- **GET** `/driver/my-profile`
- **Description**: Get current driver's profile
- **Response**: `DriverDto` object
- **Status**: 200 OK

#### 6. Get All Rides
- **GET** `/driver/all-rides`
- **Description**: Get paginated list of driver's rides
- **Query Parameters**:
  - `pageOffset` (optional): Page number (default: 0)
  - `pageSize` (optional): Page size (default: 10)
- **Response**: `Page<RideDto>` object
- **Status**: 200 OK

#### 7. Rate Rider
- **POST** `/driver/rate-rider/{ride-id}/{rating}`
- **Description**: Rate a rider after ride completion
- **Path Parameters**:
  - `ride-id`: ID of the completed ride
  - `rating`: Rating value (1-5)
- **Response**: `RiderDto` object
- **Status**: 200 OK

---

## Data Transfer Objects (DTOs)

### UserDto
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "roles": ["RIDER"]
}
```

### DriverDto
```json
{
  "id": 1,
  "rating": 4.5,
  "isAvailable": true,
  "vehicleId": "ABC123",
  "user": {
    "id": 1,
    "name": "John Driver",
    "email": "driver@example.com"
  },
  "currentLocation": {
    "latitude": 28.7041,
    "longitude": 77.1025
  }
}
```

### RiderDto
```json
{
  "id": 1,
  "rating": 4.2,
  "user": {
    "id": 1,
    "name": "John Rider",
    "email": "rider@example.com"
  }
}
```

### RideDto
```json
{
  "id": 1,
  "pickupLocation": {
    "latitude": 28.7041,
    "longitude": 77.1025
  },
  "destinationLocation": {
    "latitude": 28.5355,
    "longitude": 77.3910
  },
  "requestTime": "2024-01-01T10:00:00Z",
  "driver": {
    "id": 1,
    "name": "John Driver",
    "rating": 4.5,
    "vehicleId": "ABC123"
  },
  "rider": {
    "id": 1,
    "name": "John Rider",
    "rating": 4.2
  },
  "rideStatus": "CONFIRMED",
  "paymentMethod": "WALLET",
  "fare": 150.00,
  "startTime": "2024-01-01T10:05:00Z",
  "endTime": "2024-01-01T10:30:00Z",
  "otp": "1234"
}
```

### RideRequestDto
```json
{
  "id": 1,
  "pickupLocation": {
    "latitude": 28.7041,
    "longitude": 77.1025
  },
  "destinationLocation": {
    "latitude": 28.5355,
    "longitude": 77.3910
  },
  "paymentMethod": "WALLET",
  "requestTime": "2024-01-01T10:00:00Z",
  "rider": {
    "id": 1,
    "name": "John Rider",
    "rating": 4.2
  },
  "fare": 150.00,
  "rideRequestStatus": "PENDING"
}
```

### PointDto
```json
{
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

---

## Enums

### RideStatus
- `CONFIRMED`: Ride has been accepted by driver
- `ONGOING`: Ride is in progress
- `ENDED`: Ride has been completed
- `CANCELLED`: Ride has been cancelled

### RideRequestStatus
- `PENDING`: Request is waiting for driver acceptance
- `ACCEPTED`: Request has been accepted
- `CANCELLED`: Request has been cancelled

### PaymentMethods
- `WALLET`: Payment through wallet
- `CASH`: Cash payment

### Roles
- `RIDER`: Regular user who can book rides
- `DRIVER`: User who can accept and fulfill rides
- `ADMIN`: System administrator

---

## Error Handling

The API uses global exception handling with the following error types:

### ResourceNotFoundException (404)
```json
{
  "data": null,
  "error": {
    "message": "Resource not found",
    "subErrors": []
  },
  "timeStamp": "2024-01-01T10:00:00Z"
}
```

### RuntimeConflictException (409)
```json
{
  "data": null,
  "error": {
    "message": "Conflict occurred",
    "subErrors": []
  },
  "timeStamp": "2024-01-01T10:00:00Z"
}
```

### AuthorizationServiceException (401)
```json
{
  "data": null,
  "error": {
    "message": "Unauthorized access",
    "subErrors": []
  },
  "timeStamp": "2024-01-01T10:00:00Z"
}
```

---

## Rate Limiting & Best Practices

1. **Authentication**: Always include JWT token in requests to protected endpoints
2. **Pagination**: Use `pageOffset` and `pageSize` parameters for list endpoints
3. **Location Data**: All location data uses WGS84 coordinate system (SRID: 4326)
4. **Timestamps**: All timestamps are in ISO 8601 format with timezone information
5. **Error Handling**: Always check the `error` field in responses for error details

---

## WebSocket Support (Future Enhancement)
Real-time features like location tracking and ride updates will be implemented using WebSocket connections in future versions.
