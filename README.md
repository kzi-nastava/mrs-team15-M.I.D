# RideNow (M.I.D)

RideNow is a multi-client ride-hailing platform with a Spring Boot backend, Angular web frontend, and Android mobile app. The system includes real-time messaging, route and ride management, notifications, and admin tooling.

## Modules

- backend/RideNow: Spring Boot API + WebSocket services
- frontend: Angular 21 web client (with SSR build target)
- mobile: Android app
- scripts: database maintenance SQL scripts

## Tech Stack

- Backend: Java 17, Spring Boot 3.5, PostgreSQL, Spring Security, WebSocket, OpenAPI
- Frontend: Angular 21, TypeScript, Bootstrap, Chart.js, Leaflet
- Mobile: Android (Gradle), Java 11, Retrofit, OkHttp, Firebase Cloud Messaging

## How It Works

### Backend Architecture

- Layered design: controllers expose REST endpoints, services hold business logic, repositories handle database access.
- DTOs are used to shape API responses and requests; entities in model/ map to PostgreSQL tables.
- Validation and custom exceptions are applied at the service/controller level.

### Authentication and Roles

- Login returns a JWT. Clients send it as `Authorization: Bearer <token>`.
- `JwtRequestFilter` validates the token and loads the user from the database.
- Role-based access is enforced in the security configuration (ADMIN, USER, DRIVER).
- Users can be logged out by invalidating the token server-side (`User.isJwtTokenValid`).

### Ride Lifecycle

- Ride estimation uses the routing service to calculate distance/time and then pricing per vehicle type.
- Ordering a ride validates scheduled time, selects the best available driver, creates the ride and passengers,
	and triggers notifications to the driver and passengers.
- Scheduled rides are limited to 5 hours in advance.
- Ride status changes drive notifications and history views for users and admins.

### Routing and Geocoding

- Routing uses OSRM for turn-by-turn geometry and duration.
- Geocoding and reverse geocoding use Nominatim (OpenStreetMap).
- Routes with stops are calculated as multiple legs and merged into one polyline.
- A startup recalculation job can rebuild stored polylines (controlled by
	`app.route.recalculation.enabled`).

### Notifications (WebSocket + Email + Push)

- Web notifications stream over `/api/notifications/websocket?token=...`.
- On connect, clients receive initial state (unresolved panic alerts for admins,
	latest notifications for users and drivers).
- Notifications are stored in the database and broadcast in real time.
- Firebase Cloud Messaging is used for mobile push notifications when configured.
- Email is sent for key events (ride assigned, started, finished, passenger added).

### Chat (Support)

- Users and drivers can open a support chat; admins can take and close chats.
- Real-time chat runs over `/api/chat/websocket/{chatId}?token=...`.
- The server sends previous messages to new WebSocket connections and broadcasts new messages
	to all active sessions in the chat.

### Background Jobs

- A scheduler runs every minute and sends reminders 15, 10, and 5 minutes before scheduled rides.

### Frontend and Mobile Clients

- Angular uses a JWT interceptor to attach the token from local storage.
- Web clients use REST for CRUD operations and WebSocket for chat/notifications.
- Android uses Retrofit/OkHttp for REST and FCM for push notifications.

## Prerequisites

- Java 17 (backend)
- Node.js + npm (frontend)
- Android Studio + Android SDK (mobile)
- PostgreSQL 14+ (local database)

## Environment Variables

Create a .env file or set environment variables for the backend:

- DB_USERNAME
- DB_PASSWORD
- EMAIL_USER
- EMAIL_PASS
- JWT_SECRET
- JWT_EXPIRATION

Firebase:

- Provide firebase-credentials.json on the backend classpath (backend/RideNow/src/main/resources)

## Running Locally

### Backend (Spring Boot)

From backend/RideNow:

1) Configure PostgreSQL database ridenow and credentials.
2) Run the app:

	./mvnw spring-boot:run

Backend listens on port 8081 by default.

Swagger UI: http://localhost:8081/swagger-ui.html
OpenAPI JSON: http://localhost:8081/v3/api-docs

### Frontend (Angular)

From frontend:

1) Install dependencies:

	npm install

2) Start dev server:

	npm start

Frontend dev server runs at http://localhost:4200.

### Mobile (Android)

Open mobile/ in Android Studio and run the app on an emulator or device.

If building from terminal:

./gradlew :app:assembleDebug

## Database Utilities

Scripts are in scripts/:

- drop_tables.sql: drops all tables for a clean reset
- reset_sequences.sql: resets PostgreSQL sequences to max IDs

## Testing

Backend tests (H2 in-memory database, port 8081):

cd backend/RideNow
./mvnw test

Frontend tests:

cd frontend
npm test

## Notes

- backend/RideNow/uploads is used for uploaded files.
- Angular SSR build is configured; see package.json scripts for serve:ssr.
