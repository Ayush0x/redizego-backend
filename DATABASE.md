# Database Schema Documentation

## Overview
This document provides an overview of the database schema used in the Redizego backend project. The database utilizes PostgreSQL with PostGIS extension for geospatial data.

## Entity Relationships
The following diagram outlines the main entities:

```
+-------------------+
|       User        |
|-------------------|
| id                |
| name              |
| email             |
| password          |
| roles             |
+-------------------+
           |  1
           |  
           | 1
+----------v----------+
|      Driver         |
|---------------------|
| id                  |
| rating              |
| isAvailable         |
| vehicleId           |
| currentLocation     |
+---------------------+

+-------------------+
|       Rider       |
|-------------------|
| id                |
| rating            |
+-------------------+

+-------------------+
|       Ride        |
|-------------------|
| id                |
| pickupLocation    |
| destinationLocation |
| requestTime       |
| fare              |
| rideStatus        |
| paymentMethod     |
+-------------------+

+-------------------+
|    RideRequest    |
|-------------------|
| id                |
| pickupLocation    |
| destinationLocation|
| requestTime       |
| fare              |
| rideRequestStatus |
| paymentMethod     |
+-------------------+

+-------------------+
|      Payment      |
|-------------------|
| id                |
| paymentMethod     |
| paymentStatus     |
| amount            |
| paymentTime       |
+-------------------+

+-------------------+
|      Wallet       |
|-------------------|
| id                |
| balance           |
+-------------------+

+----------------------+
| WalletTransactions   |
|----------------------|
| id                   |
| transactionTime      |
| transactionAmount    |
| transactionType      |
+----------------------+
```

## Entities Description

### User
- **Fields**: ID, Name, Email, Password, Roles
- **Description**: Core user entity representing both riders and drivers

### Driver
- **Fields**: ID, Rating, Availability, Vehicle ID, Current Location
- **Description**: Detail for driver users with additional attributes specific to their role

### Rider
- **Fields**: ID, Rating
- **Description**: Simple entity representing the customer requesting rides

### Ride
- **Fields**: ID, Pickup/Drop Locations, Request Time, Fare, Status, Payment Method
- **Description**: Complete information about each ride journey

### RideRequest
- **Fields**: ID, Pickup/Drop Locations, Request Time, Fare, Status, Payment Method
- **Description**: Initial ride request from riders before a driver accepts the ride

### Payment
- **Fields**: ID, Method, Status, Amount, Time
- **Description**: Tracks all payments associated with rides

### Wallet
- **Fields**: ID, Balance
- **Description**: Managed with user's balance and wallet transactions

### WalletTransactions
- **Fields**: ID, Transaction Time, Amount, Type
- **Description**: Records all wallet transactions of a user

## Geospatial Data
- **SRID**: 4326 (WGS 84 - for GPS)
- **Stored**: Using PostGIS extension in PostgreSQL
- **Tables with geospatial data**: `Ride`, `RideRequest`, `Driver`

## Indices
To improve query performance, several indices are applied across entities, particularly for email, foreign keys, and geospatial data.

```
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_driver_vehicle_id ON drivers(vehicle_id);
CREATE INDEX idx_ride_rider ON rides(rider_id);
CREATE INDEX idx_ride_driver ON rides(driver_id);
CREATE INDEX idx_rider ON ride_requests(rider_id);
```

## Future Considerations
- **Sharding**: As user growth increases, sharding may be considered for the `Ride` and `Payment` tables.
- **Caching**: Explore potential benefits of caching frequent read-heavy queries to alleviate database load.
