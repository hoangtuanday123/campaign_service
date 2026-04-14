# Real-Time Promotion Campaign Platform

## 1. Project Overview

This project is a microservices-based real-time promotion campaign platform designed for merchants who need to launch, manage, and analyze promotional campaigns, and for end-users who receive and redeem those promotions in real time.

The platform separates campaign lifecycle management, promotion eligibility, user management, gateway security, and analytics into focused services so each capability can scale and evolve independently. It is designed to demonstrate production-oriented backend engineering patterns such as service decomposition, event-driven communication, caching, and API security.

### Core business capabilities

- Merchant onboarding and user identity management
- Campaign creation, activation, deactivation, and quota control
- Real-time promotion validation and redemption checks
- Campaign event publishing and downstream analytics processing
- API Gateway routing, JWT validation, and Redis-backed rate limiting
- Analytics aggregation for campaign performance reporting
- Optional extensibility for notifications and additional asynchronous consumers

## 2. System Architecture

### Architectural style

The system follows a microservices architecture where each service owns a clear business boundary and its own data access concerns:

- `API Gateway` is the single public entry point
- `User Service` manages authentication and user profiles
- `Campaign Service` manages campaign lifecycle and publishes lifecycle events
- `Promotion Engine` evaluates eligibility and tracks promotion interactions
- `Analytics Service` consumes campaign events and stores analytics views
- `Notification Service` is optional and can be added as an event consumer for outbound communication

### Service responsibilities

#### API Gateway

- Routes external traffic to downstream services
- Rewrites public paths to internal service paths
- Validates JWT access tokens for protected endpoints
- Forwards authenticated user context through headers
- Applies Redis-backed rate limiting for non-public routes

#### User Service

- Registers and authenticates users
- Issues JWT access tokens
- Stores user profile data
- Supports profile retrieval, update, and soft delete
- Exposes API-key-based access for trusted internal service-to-service calls

#### Campaign Service

- Creates and updates campaigns
- Activates and deactivates campaigns
- Maintains quota and status state
- Caches active campaign metadata in Redis
- Publishes lifecycle events to Kafka on campaign state changes

#### Promotion Engine

- Validates user eligibility through User Service
- Reads campaign state from Redis and falls back to Campaign Service
- Enforces one-time usage and quota safety through Redis
- Publishes promotion-related interaction events such as apply, view, and click
- Keeps local campaign cache warm by consuming campaign lifecycle events

#### Analytics Service

- Consumes `campaign-events` from Kafka
- Initializes and finalizes campaign analytics documents in MongoDB
- Exposes secured analytics read APIs
- Provides a base for impression, click, and conversion aggregation

### Communication patterns

- `REST APIs` are used for synchronous request/response flows that require immediate validation or data retrieval
- `Kafka` is used for asynchronous event-driven communication, especially for campaign lifecycle propagation and downstream processing
- `Redis` is used for low-latency caching, rate limiting, and atomic promotion usage enforcement

### Architecture diagram

```text
                        +----------------------+
                        |      Merchants       |
                        |      End Users       |
                        +----------+-----------+
                                   |
                                   v
                        +----------------------+
                        |     API Gateway      |
                        | JWT + Rate Limit     |
                        +----+----+----+-------+
                             |    |    |
               REST          |    |    | REST
                             |    |    |
                             v    v    v
                   +---------+  +------------------+
                   | User    |  | Campaign Service |
                   | Service |  | MySQL + Redis    |
                   | MySQL   |  +---------+--------+
                   | MongoDB |            |
                   +----+----+            | Kafka: campaign-events
                        |                 v
                        | REST   +----------------------+
                        +------> | Promotion Engine     |
                                 | Redis + Kafka        |
                                 +----+-----------+-----+
                                      |           |
                                      |           | Kafka: promotion-events
                                      |           |
                                      v           v
                            +----------------+   +----------------------+
                            | Analytics      |   | Notification Service |
                            | Service        |   | Optional Consumer    |
                            | MongoDB        |   +----------------------+
                            +----------------+
```

## 3. Tech Stack

### Core technologies

- `Java 21+`
  - Chosen for modern language features, strong performance, and long-term support alignment for backend systems
- `Spring Boot`
  - Used to build production-ready microservices quickly with strong ecosystem support for web, security, data, Kafka, and observability
- `Spring Cloud Gateway`
  - Used as the API Gateway for centralized routing, authentication enforcement, and rate limiting
- `MySQL`
  - Used for transactional relational data such as users and campaigns
- `MongoDB`
  - Used for flexible document-style storage such as activity logs and analytics documents
- `Redis`
  - Used for hot-path caching, quota state, idempotent promotion usage checks, and API rate limiting
- `Apache Kafka`
  - Used for event-driven communication and decoupled downstream processing

### Supporting libraries and patterns in the repository

- `Spring Security` for JWT and API key authentication
- `Spring Data JPA` for relational persistence
- `Spring Data MongoDB` for document persistence
- `Spring Kafka` for event production and consumption
- `Flyway` for campaign schema migrations
- `Springdoc OpenAPI` for Swagger UI on service APIs

## 4. Features

### Campaign Management

- Create campaigns with schedule, quota, and rule identifiers
- Update campaign metadata before or during lifecycle transitions
- Activate and deactivate campaigns explicitly
- Maintain remaining quota and active status
- Cache active campaign snapshots in Redis for downstream fast reads

### Real-Time Promotion Engine

- Validate that the user exists and is active
- Validate that the campaign exists, is active, is within time window, and has remaining quota
- Claim usage atomically to avoid duplicate redemption
- Publish promotion interaction events for downstream consumers
- Fall back gracefully to source-of-truth services when cache is cold

### Analytics Tracking

- Consume campaign activation and deactivation events
- Create and finalize campaign analytics documents
- Expose campaign analytics and report endpoints
- Provide a document model that can grow to include more real-time metrics

### API Gateway

- Centralized routing to all backend services
- JWT validation before requests hit protected services
- Authenticated user header forwarding for downstream trust
- Redis-backed rate limiting on protected endpoints
- Cleaner client-facing URLs through path rewriting

## 5. API Documentation

All examples below can be called directly on each service. Gateway-facing routes are available for user profile, campaign, promotion, and analytics endpoints; authentication endpoints are currently exposed directly by User Service.

### User Service

#### Register user

**Endpoint**

```http
POST /api/v1/auth/register
```

**Request**

```json
{
  "username": "merchant_admin",
  "email": "merchant@example.com",
  "password": "StrongPass123",
  "phoneNumber": "+84901234567",
  "roles": ["MERCHANT"]
}
```

**Response**

```json
{
  "timestamp": "2026-04-14T10:00:00Z",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": "7cf9d01d-756b-4cdd-bbbf-354818f69fd3",
      "username": "merchant_admin",
      "email": "merchant@example.com",
      "phoneNumber": "+84901234567",
      "status": "ACTIVE",
      "roles": ["MERCHANT"],
      "createdAt": "2026-04-14T10:00:00Z",
      "updatedAt": "2026-04-14T10:00:00Z"
    }
  }
}
```

#### Login

**Endpoint**

```http
POST /api/v1/auth/login
```

**Request**

```json
{
  "usernameOrEmail": "merchant@example.com",
  "password": "StrongPass123"
}
```

### Campaign Service

#### Create campaign

**Endpoint**

```http
POST /api/v1/campaigns
```

**Request**

```json
{
  "name": "Summer Cashback Blast",
  "startTime": "2026-05-01T00:00:00Z",
  "endTime": "2026-05-31T23:59:59Z",
  "quota": 5000,
  "ruleId": "RULE-CASHBACK-10"
}
```

**Response**

```json
{
  "timestamp": "2026-04-14T10:05:00Z",
  "status": 201,
  "message": "Campaign created successfully",
  "data": {
    "id": "624c9392-b24d-4dce-ac53-6d65d12c9988",
    "name": "Summer Cashback Blast",
    "status": "DRAFT",
    "startTime": "2026-05-01T00:00:00Z",
    "endTime": "2026-05-31T23:59:59Z",
    "quota": 5000,
    "usedCount": 0,
    "remainingQuota": 5000,
    "ruleId": "RULE-CASHBACK-10",
    "createdAt": "2026-04-14T10:05:00Z",
    "updatedAt": "2026-04-14T10:05:00Z"
  }
}
```

#### Activate campaign

**Endpoint**

```http
PUT /api/v1/campaigns/{id}/activate
```

### Promotion Engine

#### Apply promotion

**Endpoint**

```http
POST /api/v1/promotions/apply
```

**Request**

```json
{
  "userId": "7cf9d01d-756b-4cdd-bbbf-354818f69fd3",
  "campaignId": "624c9392-b24d-4dce-ac53-6d65d12c9988"
}
```

**Response**

```json
{
  "timestamp": "2026-04-14T10:10:00Z",
  "status": 200,
  "message": "Promotion evaluated successfully",
  "data": {
    "eligible": true,
    "message": "Promotion applied successfully"
  }
}
```

#### Track promotion view

**Endpoint**

```http
POST /api/v1/promotions/view
```

**Request**

```json
{
  "userId": "7cf9d01d-756b-4cdd-bbbf-354818f69fd3",
  "campaignId": "624c9392-b24d-4dce-ac53-6d65d12c9988"
}
```

### Analytics Service

#### Get campaign analytics

**Endpoint**

```http
GET /api/v1/analytics/campaign/{campaignId}
```

**Response**

```json
{
  "timestamp": "2026-04-14T10:15:00Z",
  "status": 200,
  "message": "Campaign analytics retrieved successfully",
  "data": {
    "campaignId": "624c9392-b24d-4dce-ac53-6d65d12c9988",
    "impressions": 1240,
    "clicks": 97,
    "conversions": 11,
    "status": "ACTIVE",
    "startedAt": "2026-05-01T00:00:00Z",
    "endedAt": null
  }
}
```

## 6. Data Flow

### Apply Promotion Flow

1. Client sends `POST /api/promotions/apply` to the API Gateway.
2. API Gateway validates JWT, applies rate limiting, and forwards the request to the Promotion Engine.
3. Promotion Engine validates the user by calling User Service.
4. Promotion Engine checks Redis for cached campaign state and usage information.
5. If campaign cache is missing, Promotion Engine retrieves campaign data from Campaign Service and refreshes Redis.
6. Promotion Engine atomically claims user usage and quota through Redis-based logic.
7. On success, Promotion Engine publishes a `promotion_applied` event to Kafka.
8. Analytics and other downstream consumers can process these events asynchronously.

### Campaign Activation Flow

1. Merchant activates a campaign through Campaign Service.
2. Campaign Service validates schedule and remaining quota.
3. Campaign Service updates campaign status in MySQL.
4. Campaign Service stores active campaign data in Redis.
5. Campaign Service publishes `campaign_activated` to Kafka.
6. Promotion Engine consumes the event and refreshes its hot-path cache.
7. Analytics Service consumes the same event and initializes the analytics document in MongoDB.

## 7. Project Structure

```text
.
|-- api_gateway/
|-- analytics_service/
|-- campaign_service/
|-- promotion_engine/
|-- user_service/
`-- README.md
```

### Service folder mapping

- `api_gateway/` -> API Gateway
- `user_service/` -> User Service
- `campaign_service/` -> Campaign Service
- `promotion_engine/` -> Promotion Engine
- `analytics_service/` -> Analytics Service

## 8. Setup & Run Instructions

### Prerequisites

- Java 21 or newer
- Maven 3.9+
- Docker Desktop or a local installation of infrastructure dependencies
- MySQL
- MongoDB
- Redis
- Kafka

### Default local ports in this repository

- `8080` -> API Gateway
- `8081` -> User Service
- `8083` -> Campaign Service
- `8084` -> Promotion Engine
- `8085` -> Analytics Service
- `3306` -> MySQL
- `27017` -> MongoDB
- `6379` -> Redis
- `19092`, `29092` -> Kafka bootstrap servers

### Clone the repository

```bash
git clone <your-repository-url>
cd campaign_service
```

### Start infrastructure

This repository currently does not include a root `docker-compose.yml`, so infrastructure can be started either with your own Docker Compose file or with existing local services using the ports above.

### Run services

Open a terminal for each service and start them independently:

```bash
cd user_service
mvn spring-boot:run
```

```bash
cd campaign_service
mvn spring-boot:run
```

```bash
cd promotion_engine
mvn spring-boot:run
```

```bash
cd analytics_service
mvn spring-boot:run
```

```bash
cd api_gateway
mvn spring-boot:run
```

### Suggested startup order

1. MySQL, MongoDB, Redis, Kafka
2. User Service
3. Campaign Service
4. Promotion Engine
5. Analytics Service
6. API Gateway

## 9. Configuration

Each service uses `application.yml` for environment-specific configuration.

### Common configuration categories

- `server.port` for service port binding
- `spring.datasource` for MySQL connections
- `spring.data.mongodb` for MongoDB connections
- `spring.data.redis` for Redis configuration
- `spring.kafka` for producer and consumer connectivity
- `security.jwt` for token signing and expiration
- `security.api-key` or `security.apikey` for trusted internal access
- `services.*.base-url` for downstream service locations

### Example configuration areas

```yaml
server:
  port: 8084

services:
  user:
    base-url: http://localhost:8081
  campaign:
    base-url: http://localhost:8083

promotion:
  events:
    topic: promotion-events
```

### Important environment variables used by the gateway

- `REDIS_HOST`
- `REDIS_PORT`
- `JWT_SECRET`
- `JWT_EXPIRATION_MILLIS`
- `USER_SERVICE_BASE_URL`
- `CAMPAIGN_SERVICE_BASE_URL`
- `PROMOTION_ENGINE_BASE_URL`
- `ANALYTICS_SERVICE_BASE_URL`
- `GATEWAY_RATE_LIMIT_MAX_REQUESTS`
- `GATEWAY_RATE_LIMIT_WINDOW_SECONDS`

## 10. Scalability & Design Decisions

### Why Redis is used

- To keep hot campaign data close to the Promotion Engine
- To enforce atomic promotion usage claims under concurrency
- To store quota-sensitive state without repeated relational reads
- To support low-latency rate limiting in the API Gateway

### Why Kafka is used

- To decouple campaign lifecycle changes from downstream consumers
- To let analytics and future services react asynchronously
- To reduce tight runtime coupling between write flows and reporting flows
- To support horizontal scaling with partitioned event consumption

### How the system handles high concurrency

- Services are stateless at the application layer and can be scaled horizontally
- Redis reduces repeated downstream round-trips on the hottest execution path
- Promotion usage is claimed atomically to avoid duplicate redemption
- Kafka absorbs bursts and smooths downstream processing
- Gateway rate limiting protects services from abusive or accidental spikes

### Stateless service design

- Business state is externalized to MySQL, MongoDB, Redis, and Kafka
- Any service instance can serve requests without relying on in-memory affinity
- This makes containerized deployment and autoscaling more practical

## 11. Trade-offs

### Why not a monolith

- A monolith would be simpler to start, but would mix campaign lifecycle, eligibility logic, analytics, and gateway concerns into a single deployable unit
- This design favors separation of concerns, independent scaling, and clearer ownership boundaries

### Why async plus sync communication

- `REST` is used where the caller needs an immediate answer, such as authentication or eligibility validation
- `Kafka` is used where eventual consistency is acceptable and loose coupling is more valuable than synchronous confirmation

### Current limitations

- There is no root-level Docker Compose setup yet
- Notification Service is architectural-ready but not implemented in this repository
- Analytics currently consumes campaign lifecycle events; richer real-time promotion event aggregation can be extended further
- Some service versions in the repository are not yet fully standardized on one Java and Spring Boot baseline

## 12. Future Improvements

- Add distributed tracing with OpenTelemetry
- Add circuit breakers and retries with Resilience4j
- Add centralized monitoring with Prometheus and Grafana
- Add structured logging and correlation IDs across services
- Add CI/CD pipelines for test, build, scan, and deployment automation
- Add a root Docker Compose file for one-command local startup
- Add Notification Service consumers for email, SMS, or push channels
- Extend analytics to consume `promotion-events` directly for richer dashboards

## 13. Interview Highlights

### Why this project is production-ready

- The architecture demonstrates clear microservice boundaries and independent service responsibilities
- It combines synchronous validation with asynchronous event-driven processing
- It uses Redis intentionally for latency-sensitive paths and concurrency control
- It uses Kafka to decouple state changes from downstream consumers
- It includes API security, rate limiting, and service-to-service trust mechanisms
- It shows realistic persistence choices by mixing relational and document databases
- It is structured in a way that supports scaling, extensibility, and operational maturity

## 14. Writing Style Summary

This README is intentionally structured to be:

- Easy to scan for recruiters and interviewers
- Clear enough for onboarding engineers
- Specific enough to discuss architectural decisions in interviews
- Professional enough to represent the project as a portfolio-ready backend platform

## 15. Quick Access

### Service endpoints

- API Gateway: `http://localhost:8080`
- User Service: `http://localhost:8081`
- Campaign Service: `http://localhost:8083`
- Promotion Engine: `http://localhost:8084`
- Analytics Service: `http://localhost:8085`

### Swagger UI

- Campaign Service: `http://localhost:8083/swagger-ui.html`
- Promotion Engine: `http://localhost:8084/swagger-ui.html`
- Analytics Service: `http://localhost:8085/swagger-ui.html`

### Example gateway routes

- Authentication is currently called directly on User Service, for example `POST http://localhost:8081/api/v1/auth/register`
- `GET /api/users/me` routes to User Service
- `POST /api/campaigns` routes to Campaign Service
- `POST /api/promotions/apply` routes to Promotion Engine
- `GET /api/analytics/campaign/{campaignId}` routes to Analytics Service
