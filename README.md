🚖 Redizego Backend – Cab Booking Platform API
Redizego is a ride-hailing backend service built with Spring Boot, designed to replicate core functionality similar to Uber. It includes secure authentication, cab management, real-time ride requests, and flexible driver-matching strategies. Future plans include offline booking support and surge pricing.

🔧 Tech Stack
Layer	Technology
Language	Java (Spring Boot)
Database	PostgreSQL + PostGIS (GeoSpatial)
Auth	JWT & OAuth2
Patterns	Strategy, Builder, Factory, Singleton
Docs	Swagger UI
🗺 Architecture & Modules
Based on the design below (see diagram in repo):

Core Modules
UserService: Signup, login, and authentication (JWT + OAuth2)
DriverService: Start/end rides, update driver status
RiderService: Request, cancel rides
RideRequestService: Handles ride request matching & fare calculation
AdminService: Onboard drivers, view system stats
RatingService: Rate rider/driver post-ride
WalletService: Add/debit funds
EmailService: Notify drivers on ride requests
🧠 Smart Matching & Fare Calculation
DriverMatchingStrategy (via Strategy Pattern):
NearestDriverStrategy
RatingBasedStrategy
CalculateFareStrategy:
GetDefaultFareStrategy
SurgePricingFareStrategy
📌 Features
✅ User authentication with JWT + OAuth2
✅ Role-based access (Admin, Rider, Driver)
✅ Driver & ride management
✅ Flexible ride-matching logic
✅ Ratings & feedback
✅ Email notifications
✅ Swagger API docs
🗺️ GeoSpatial support for driver & ride tracking
📂 Folder Structure (Planned)
redizego/ ├── src/ │ ├── controller/ │ ├── service/ │ ├── model/ │ ├── repository/ │ ├── config/ │ └── strategy/ ├── resources/ │ └── application.yml ├── pom.xml └── README.md

🧪 Testing & Profiles Profiles supported: dev, prod

Use @Profile for env-specific configurations

Swagger available at: http://localhost:8080/swagger-ui.html

🧭 Roadmap JWT/OAuth login system

Cab booking API

Admin dashboard

Offline booking (via GPS/SMS)

Notification service with WebSockets

Payment gateway integration

🧑‍💻 Author Ayush Sharma Java Backend Developer | Spring Boot Enthusiast
