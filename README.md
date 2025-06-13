# redizego-backend
🚖 Redizego is a Spring Boot-powered backend API for a cab booking platform with login, signup, user and cab management, and booking features. Future versions will support offline GPS-based booking.


# 🚖 Redizego Backend – Cab Booking Platform API

Redizego is a ride-hailing backend service built with **Spring Boot**, designed to replicate core functionality similar to Uber. It includes secure authentication, cab management, real-time ride requests, and flexible driver-matching strategies. Future plans include offline booking support and surge pricing.

---

## 🔧 Tech Stack

| Layer        | Technology                         |
|--------------|------------------------------------|
| Language     | Java (Spring Boot)                 |
| Database     | PostgreSQL + PostGIS (GeoSpatial)  |
| Auth         | JWT & OAuth2                       |
| Patterns     | Strategy, Builder, Factory, Singleton |
| Docs         | Swagger UI                         |

---

## 🗺 Architecture & Modules

Based on the design below (see diagram in repo):

### Core Modules

- **UserService**: Signup, login, and authentication (JWT + OAuth2)
- **DriverService**: Start/end rides, update driver status
- **RiderService**: Request, cancel rides
- **RideRequestService**: Handles ride request matching & fare calculation
- **AdminService**: Onboard drivers, view system stats
- **RatingService**: Rate rider/driver post-ride
- **WalletService**: Add/debit funds
- **EmailService**: Notify drivers on ride requests

---

## 🧠 Smart Matching & Fare Calculation

- **DriverMatchingStrategy** (via Strategy Pattern):
  - `NearestDriverStrategy`
  - `RatingBasedStrategy`
- **CalculateFareStrategy**:
  - `GetDefaultFareStrategy`
  - `SurgePricingFareStrategy`

---

## 📌 Features

- ✅ User authentication with JWT + OAuth2
- ✅ Role-based access (Admin, Rider, Driver)
- ✅ Driver & ride management
- ✅ Flexible ride-matching logic
- ✅ Ratings & feedback
- ✅ Email notifications
- ✅ Swagger API docs
- 🗺️ GeoSpatial support for driver & ride tracking

---

## 📂 Folder Structure (Planned)

