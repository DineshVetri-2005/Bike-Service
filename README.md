# RevTune — Bike Service Booking Management System

A lightweight Spring Boot MVC backend where customers can book bike services,
manage their vehicles, and track service status. Built for a final-year mini project.

## Tech Stack
- Java 17
- Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Validation)
- SQLite (via sqlite-jdbc + hibernate-community-dialects)
- Maven
- Lombok
- Postman (for testing — collection included)

## Package Structure
```
com.revtune
├── controller     UserController, BikeController, ServiceController, BookingController
├── service        UserService, BikeService, ServiceCatalogService, BookingService
├── repository     UserRepository, BikeRepository, ServiceRepository, BookingRepository
├── model           User, Bike, Service, Booking, BookingStatus
├── dto            Request/Response DTOs
├── exception      GlobalExceptionHandler + custom exceptions
└── config         DataSeeder (seeds sample services on first run)
```

> Note: the service class for the `Service` entity is named `ServiceCatalogService`
> (not `ServiceService`) simply to avoid confusion with Spring's own
> `@Service` stereotype annotation — it's still wired with `@Service` as required.

## Frontend

A static dashboard lives in `src/main/resources/static/` (`index.html`, `css/style.css`,
`js/app.js`) and is served automatically by Spring Boot at `http://localhost:8080/` — no
extra configuration needed. It's a plain HTML/CSS/JS console (no build step) that talks
to the REST API with `fetch()`: register, sign in, manage your garage, browse the service
menu, and create/track bookings, all from the browser.

## Getting Started

### Prerequisites
- JDK 17+
- Maven 3.6+

### Run the application
```bash
mvn clean install
mvn spring-boot:run
```

The app starts on `http://localhost:8080` and creates a `revtune.db` SQLite file
in the project root on first run. Tables are auto-created by Hibernate
(`spring.jpa.hibernate.ddl-auto=update`), and five sample services (Engine Oil
Change, General Service, Brake Inspection, Chain Maintenance, Battery Check)
are seeded automatically if the `services` table is empty.

## API Endpoints

### User APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Login with email + password |
| GET | `/api/users/{id}` | Get user profile |
| PUT | `/api/users/{id}` | Update user details |

### Bike APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bikes` | Add a bike |
| GET | `/api/bikes/user/{userId}` | List bikes for a user |
| PUT | `/api/bikes/{id}` | Update a bike |
| DELETE | `/api/bikes/{id}` | Delete a bike |

### Service APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/services` | Create a service (admin) |
| GET | `/api/services` | List all services |
| GET | `/api/services/{id}` | Get a service by id |

### Booking APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create a booking |
| GET | `/api/bookings/user/{userId}` | List bookings for a user |
| GET | `/api/bookings/{id}` | Get a booking by id |
| PUT | `/api/bookings/{id}/status` | Update booking status |
| DELETE | `/api/bookings/{id}` | Delete a booking |

`totalAmount` on a booking is set automatically from the selected service's price.
Valid `status` values: `BOOKED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.

## Error Handling
All errors return a consistent JSON shape via `GlobalExceptionHandler`:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 5",
  "timestamp": "2026-07-30T10:15:00"
}
```
- `404` — resource not found
- `409` — duplicate resource (e.g. email or bike number already registered)
- `401` — invalid login credentials
- `400` — validation failure (includes a `validationErrors` map field-by-field)
- `500` — unexpected server error

## Testing with Postman
Import `postman/RevTune.postman_collection.json` into Postman. It includes
sample requests and example responses for every endpoint, using a `baseUrl`
collection variable (defaults to `http://localhost:8080`).

Suggested test flow:
1. Register a user → note the returned `id`
2. Login with the same credentials
3. Add a bike for that user
4. Get all services (or create your own)
5. Create a booking using the user, bike, and service ids
6. Update the booking status as it moves through the workflow
