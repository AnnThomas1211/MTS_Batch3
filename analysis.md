# Project Architecture Analysis

## Project Overview
The repository is structured as a full-stack money transfer system with two main application areas:

- `backend/` - Spring Boot-based Java microservice
- `frontend/mts-ui/` - Angular web application

There is also a PDF requirement document present at the root, but no separate `database/`, `snowflake/`, or repository-level `README.md` in the workspace.

## Backend Architecture

### Technology stack
- Java 17
- Spring Boot 3.0.13
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring AOP dependency included
- MySQL connector
- SpringDoc OpenAPI UI dependency included
- Maven build

### Package structure
- `com.fidelity.mts.controller` - REST controllers
- `com.fidelity.mts.service` - service layer interfaces and implementations
- `com.fidelity.mts.repo` - Spring Data JPA repositories
- `com.fidelity.mts.domain.model` - JPA entities
- `com.fidelity.mts.domain.enums` - enum definitions
- `com.fidelity.mts.domain.exception` - custom exceptions and global exception handler
- `com.fidelity.mts.application.dto` - request/response DTO records
- `com.fidelity.mts.security` - Spring Security configuration

### Key backend components
- `MoneyTransferSystemApplication` - Spring Boot entry point
- `TransferController` - handles `/api/v1/transfers`
- `AccountController` - handles `/api/v1/accounts` APIs
- `TransferServiceImpl` - core transfer flow, validation, idempotency, debit/credit logic
- `AccountServiceImpl` - account retrieval, balance lookup, transaction history
- `RewardService` - reward evaluation logic invoked during transfers
- `Account` entity - account lifecycle, active status, debit/credit methods, optimistic locking via `@Version`
- `TransactionLog` entity - transfer audit trail and idempotency record
- Custom exceptions for validation, account not found, account inactive, insufficient balance, duplicate transfer
- `GlobalExceptionHandler` maps exceptions to HTTP status codes and structured error responses

### Data flow
1. Client sends POST `/api/v1/transfers` with `TransferRequest`
2. `TransferServiceImpl.transfer()` validates the request and checks idempotency
3. Account and transaction persistence is handled via JPA repositories
4. A `TransactionLog` audit record is persisted for success or failure
5. Response is returned via `TransferResponse`

### Security and configuration
- `SpringSecurityConfig` configures HTTP Basic auth for all requests
- CORS is enabled for `http://localhost:4200`
- `application.properties` includes MySQL connection settings and default Basic auth credentials
- No JWT or password hashing implementation is present

## Frontend Architecture

### Technology stack
- Angular 21
- TypeScript
- Bootstrap CSS
- RxJS
- Angular Forms
- Angular Router
- Angular SSR support present via `@angular/platform-server` and server entry files

### Frontend structure
- `src/app/` - main Angular application source
- `components/` - UI screens and widgets
- `service/` - application services for HTTP integration and state
- `guards/` - route guard for authentication
- `interceptors/` - HTTP interceptor for auth header injection
- `models/` - TypeScript data models
- `enums/` - enum values used in models and UI

### Key frontend components
- `LoginComponent` - login form, user credentials, account ID input
- `DashboardComponent` - account summary and balance display
- `TransferComponent` - transfer form, amount validation, idempotency key generation, submit flow
- `HistoryComponent` - transaction history view
- `ProfileComponent` and `RewardComponent` present but not central to the core flow
- `TopNavbarComponent` - app navigation and logout control

### Frontend services
- `AuthService` - manages login state, token storage in `localStorage`, account ID storage
- `AuthInterceptor` - appends `Authorization: Basic` header to outgoing HTTP requests
- `AccountService` - fetches account, balance, and transaction history from backend APIs
- `TransferService` - performs `POST /api/v1/transfers`

### Routing
- `/login` - public login page
- `/dashboard` - protected dashboard
- `/transfer` - protected fund transfer page
- `/history` - protected transaction history
- `/profile` - protected profile page
- `/rewards` - protected lazy-loaded reward component
- wildcard routes redirect to dashboard

### Frontend behavior
- Login stores base64-encoded credentials and account ID in browser storage
- Auth guard blocks protected pages when not logged in
- HTTP interceptor sends Basic auth header automatically
- Transfer component generates an idempotency key per request

## Missing or incomplete elements

### Missing from current repo relative to the full requirement spec
- No `database/` folder with schema and seed SQL scripts
- No `snowflake/` folder or Snowflake analytics scripts
- No repository-level `README.md`
- No `docs/` folder seen in the workspace
- No explicit branch strategy or Git module artifacts visible
- No `analysis.md` or similar architecture documentation until now

### Backend gaps
- `spring-boot-starter-aop` is present, but there is no aspect class implementing AOP logging
- Security is Basic auth only, not JWT or BCrypt password hashing
- `application.properties` contains hard-coded DB and credentials, which is not production-safe
- No explicit database migration scripts (Flyway/Liquibase or SQL files)
- No dedicated Snowflake analytics integration or ETL support

### Frontend gaps
- Authentication is client-side only and not verified against backend login API
- No explicit backend login endpoint exists; `AuthService` only stores credentials locally
- UI flow appears complete in core pages, but there may be missing visual screens for transfer confirmation or dashboard details based on spec
- No project-level documentation or deployment instructions in frontend root

## Suggested improvements

1. Add a root-level `README.md` describing how to start backend and frontend locally.
2. Add `database/schema.sql` and `database/seed-data.sql` for MySQL table creation and sample data.
3. Add `snowflake/` scripts for warehouse/schema/ETL/query examples.
4. Implement an AOP logging aspect class or remove the unused AOP dependency.
5. Replace the current Basic auth stub with a real backend login endpoint and secure password storage.
6. Add a `docs/` folder or architecture documentation for the full training-aligned project.
7. Add tests for backend service logic and frontend components where coverage is missing.
8. Remove sensitive credentials from `application.properties` and use environment variables or configuration profiles.

## Summary
The repository already contains a solid Spring Boot backend and Angular frontend with fundamental money transfer flows implemented. However, the current codebase is missing several required artifacts from the full project specification, especially database scripts, Snowflake analytics support, and documentation.
