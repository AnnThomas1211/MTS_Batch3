# Q&A Documentation

This document contains questions and answers from the capstone project discussion.

---

## Q6: What does `SpringSecurityConfig` do?

### Answer:

This class configures Spring Security for your application (`backend/MoneyTransferSystem/src/main/java/com/fidelity/mts/security/SpringSecurityConfig.java`). Key points:

- `@Configuration` + `@EnableWebSecurity`: registers security configuration with Spring.
- `SecurityFilterChain` bean: defines the HTTP security pipeline.
    - `http.csrf(csrf->csrf.disable())`: disables CSRF protection (common for APIs where clients are not browsers or when using tokens). Be careful in production.
    - `http.cors(Customizer.withDefaults())` + `CorsConfigurationSource` bean: enables and configures CORS for `http://localhost:4200` so your Angular frontend can call the API.
    - `authorizeHttpRequests(...)`: permits all `OPTIONS` requests (preflight), and requires authentication for other requests.
    - `http.httpBasic(Customizer.withDefaults())`: enables HTTP Basic auth (username/password via Authorization header).

Why it matters:
- Controls who can access your endpoints and how they authenticate.
- CORS settings allow local frontend development to call the backend.
- HTTP Basic is simple for development; consider stronger mechanisms (JWT/OAuth2) for production.

## Q7: How does Spring Data JPA (`JpaRepository`) work?

### Answer:

Spring Data JPA provides repository interfaces (like `JpaRepository<T, ID>`) that you extend to get data access without writing implementations.

- **Auto-implementation**: At runtime, Spring creates a proxy implementation for your interface and provides standard CRUD methods (`save`, `findById`, `findAll`, `deleteById`, etc.).
- **Derived query methods**: Methods like `findByAccountIdOrderByCreatedAtDesc` are parsed by Spring and turned into SQL/JPA queries automatically based on the method name.
- **Custom queries**: Use `@Query` for JPQL/SQL when method names are insufficient.
- **Paging & sorting**: `JpaRepository` supports `Pageable` and `Sort` parameters for scalable queries.
- **Transactions**: Repository methods run in transactions; service methods often use `@Transactional` to group operations atomically.
- **Injection**: Repositories are Spring beans — inject them into services/controllers using constructor injection.

Example usage in a service:

```java
@Service
public class AccountService {
    private final AccountRepository repo;
    public AccountService(AccountRepository repo) { this.repo = repo; }

    public Account get(long id) { return repo.findById(id).orElseThrow(...); }
    public Account save(Account a) { return repo.save(a); }
}
```

Benefits:
- Less boilerplate SQL/DAO code.
- Standardized patterns for queries.
- Easy to extend with custom methods where needed.

---

## Q8: What does `@SpringBootApplication` do?

### Answer:

`@SpringBootApplication` is the main annotation that marks a class as the entry point for a Spring Boot application. It's a convenience annotation that combines three things:

1. **`@Configuration`**: Marks the class as a source of bean definitions (tells Spring this is a config class).
2. **`@EnableAutoConfiguration`**: Tells Spring Boot to automatically configure the app based on dependencies on the classpath (e.g., if you have `spring-web`, it enables web support).
3. **`@ComponentScan`**: Tells Spring to scan for `@Component`, `@Service`, `@Controller`, `@Repository`, etc. in the current package and subpackages.

### In your project:

```java
@SpringBootApplication
public class MoneyTransferSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyTransferSystemApplication.class, args);
    }
}
```

- The `@SpringBootApplication` annotation on the class tells Spring Boot: "This is the main class; scan and configure everything from here."
- `SpringApplication.run(...)` starts the entire Spring context, loads all beans, and starts the embedded server (e.g., Tomcat).

### Why it matters:

- Without this annotation, you'd need to manually add `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan` separately.
- It's the minimal code to start a Spring Boot app.
- The package containing this class becomes the base for component scanning, so all your services, repositories, controllers should be in this package or subpackages.

---

## Q2: What are the common controller annotations and what is `ResponseEntity`?

### Answer:

Below are common Spring MVC / Spring Boot controller annotations and concise explanations of their purpose and usage.

- **`@RestController`**: Combines `@Controller` + `@ResponseBody`. Methods return objects serialized (usually JSON) as the HTTP response body.
- **`@Controller`**: Traditional MVC controller (returns views); use `@ResponseBody` on methods to return raw bodies.
- **`@RequestMapping`**: Class- or method-level route mapping (path, `method`, `produces`, `consumes`).
- **`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`**: Shortcuts for `@RequestMapping(method = ...)`.
- **`@PathVariable`**: Binds a path segment to a method parameter (e.g., `/accounts/{id}` → method param `@PathVariable Long id`).
- **`@RequestParam`**: Binds query parameters (`?page=2`) to method params; supports `required=false` and `defaultValue`.
- **`@RequestBody`**: Deserializes the HTTP request body (JSON) into a Java object; commonly used for `POST`/`PUT`.
- **`@Valid` / `@Validated`**: Triggers bean validation (Jakarta/Bean Validation) for `@RequestBody` or method params; validation failures raise exceptions you handle.
- **Validation annotations** (e.g., `@NotNull`, `@DecimalMin`): Placed on DTO fields to enforce constraints during `@Valid` validation.
- **`@ResponseStatus`**: Declares the HTTP status to return for a method or an exception class (e.g., `@ResponseStatus(HttpStatus.NOT_FOUND)`).
- **`@ResponseBody`**: Instructs Spring to write the return value as the response body (implicit when using `@RestController`).
- **`@RequestHeader`**: Reads a specific HTTP header into a parameter (e.g., idempotency key).
- **`@CrossOrigin`**: Configures CORS for controller or method.
- **`@ExceptionHandler` / `@ControllerAdvice`**: Handle exceptions centrally and return custom responses (often `ErrorResponse` or `ResponseEntity<ErrorResponse>`).
- **Dependency injection annotations (`@Autowired` / constructor injection`)**: Inject services/repositories into controllers; prefer constructor injection.

### `ResponseEntity` — what and why
- **What**: `ResponseEntity<T>` is an HTTP response wrapper that contains body (`T`), HTTP status, and headers.
- **Why use it**:
    - Full control of HTTP status codes (201, 204, 400, 404, etc.).
    - Add response headers (e.g., `Location` after creating a resource).
    - Return no body with appropriate status (`204 No Content`).
    - Useful in controllers and exception handlers for precise REST semantics.
- **Common usages**:
    - `return ResponseEntity.ok(body);`
    - `return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/accounts/1").body(createdDto);`
    - `return ResponseEntity.noContent().build();`
    - `return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);`
- **Returning plain object vs `ResponseEntity`**:
    - Plain object → implicit `200 OK` (unless `@ResponseStatus` applied).
    - `ResponseEntity` → explicit status + headers; better for REST APIs.

---

## Q1: What is a DTO? What does it do? What DTOs have I created?

### Answer:

### What is a DTO?
A **DTO (Data Transfer Object)** is a simple object that carries data between different layers of an application. It's mainly used to transfer data from the backend (server) to the frontend (client) or vice versa.

### What does it do?
- **Decouples layers**: The frontend doesn't need to know about your internal database models. You send only the data you want.
- **Reduces data sent**: You can include/exclude fields based on what the client needs.
- **Validation**: You can add validation rules to ensure data is correct before processing.
- **Security**: You can hide sensitive fields (like database IDs) or internal logic from the client.

### DTOs You've Created:

#### 1. **AccountResponse**
```java
public record AccountResponse(
    long id,
    String holderName,
    BigDecimal balance,
    AccountStatus status,
    LocalDateTime lastUpdated
)
```
**Purpose**: Sends account information to the frontend when a user views their account details.
**Fields**: Account ID, holder's name, current balance, account status, and last update timestamp.

#### 2. **TransferRequest**
```java
public record TransferRequest(
    @NotNull Long fromAccountId,
    @NotNull Long toAccountId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull UUID idempotencyKey
)
```
**Purpose**: Receives money transfer data from the frontend.
**Fields**: Source account, destination account, transfer amount, and a unique key to prevent duplicate transfers.

#### 3. **TransferResponse**
```java
public record TransferResponse(
    String transactionId,
    TransactionStatus status,
    String message,
    long debitedFrom,
    long creditedTo,
    String fromAccountName,
    String toAccountName,
    BigDecimal amount
)
```
**Purpose**: Sends the result of a transfer operation back to the frontend.
**Fields**: Transaction ID, status (success/failed), message for the user, and details about which accounts were involved.

#### 4. **ErrorResponse**
```java
public record ErrorResponse(String error, String message)
```
**Purpose**: Sends error information to the frontend when something goes wrong.
**Fields**: Error type and description.

---

### Keywords Explained:

| Keyword | Meaning |
|---------|---------|
| **record** | A special Java class (Java 14+) designed for holding immutable data. Automatically generates getters, toString(), equals(), and hashCode(). |
| **@NotNull** | Validation annotation that ensures the field is not null. Prevents empty data from being sent. |
| **@DecimalMin("0.01")** | Validation annotation that ensures the amount is at least 0.01 (prevents zero or negative transfers). |
| **BigDecimal** | A Java class for precise decimal numbers (important for money to avoid floating-point errors). |
| **LocalDateTime** | A Java class for date and time without timezone information. |
| **UUID** | Universally Unique Identifier - a unique code to identify the transaction (prevents duplicate transfers). |
| **AccountStatus / TransactionStatus** | Enums (predefined values) that define the possible states (e.g., ACTIVE, PENDING, COMPLETED). |
| **public static** (in fromAccount method) | A helper method that converts an Account object to an AccountResponse (only in AccountResponse). |

---

## Q4: What does `enum` mean?

### Answer:

An `enum` is a Java type that defines a fixed set of allowed values. Use it when a variable should only hold one of a small number of options.

### In your project:
- `AccountStatus` values: `ACTIVE`, `LOCKED`, `CLOSED`
- `TransactionStatus` values: `SUCCESS`, `FAILED`

### Why enums are useful:
- They make code easier to read.
- They prevent invalid values.
- They let the compiler enforce that only valid options are used.
- They work well in DTOs, entities, APIs, and validation.

### Example:
- `account.getStatus()` can only return `ACTIVE`, `LOCKED`, or `CLOSED`.
- `response.getStatus()` can only return `SUCCESS` or `FAILED`.

---

## Q3: What do these repository files mean and what do they do?

### Answer:

These files are Spring Data JPA repository interfaces. They provide database access for specific entities and let Spring automatically generate query methods for common operations.

### `AccountRepository.java`
```java
public interface AccountRepository extends JpaRepository<Account, Long> {
}
```
- Manages the `Account` entity.
- `JpaRepository<Account, Long>` provides built-in CRUD operations such as `save()`, `findById()`, `findAll()`, and `deleteById()`.
- `Long` is the type of the entity's primary key.

### `RewardLedgerRepository.java`
```java
public interface RewardLedgerRepository extends JpaRepository<RewardLedger, Long> {
    List<RewardLedger> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardLedger r WHERE r.accountId = :accountId")
    int sumPointsByAccountId(@Param("accountId") Long accountId);
}
```
- Manages the `RewardLedger` entity.
- `findByAccountIdOrderByCreatedAtDesc(...)` is a derived query method that returns reward entries for an account sorted newest first.
- The `@Query` method calculates total reward points for an account and returns `0` when there are no records.

### `TransactionLogRepository.java`
```java
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
    Optional<TransactionLog> findByIdempotencyKey(UUID idempotencyKey);
    List<TransactionLog> findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(long fromAccountId, long toAccountId);
}
```
- Manages the `TransactionLog` entity.
- Uses `UUID` as the primary key type.
- `findByIdempotencyKey(...)` finds an existing transaction by its idempotency key to prevent duplicate processing.
- `findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(...)` retrieves transaction history where the account is either sender or receiver, ordered newest first.

### Why these repository files exist:
- They separate database access from business logic.
- They keep service/controller code clean.
- They let Spring generate database queries automatically.
- Custom methods allow specific queries when default CRUD operations are not enough.

---

## Q5: How does the global exception handler work?

### Answer:

Your global exception handler is in `backend/MoneyTransferSystem/src/main/java/com/fidelity/mts/domain/exception/GlobalExceptionHandler.java`.

- `@ControllerAdvice` makes the class apply to all controllers.
- `@ExceptionHandler(SomeException.class)` tells Spring which method should handle each exception type.
- Each handler returns a `ResponseEntity<ErrorResponse>` with a specific HTTP status.

### What happens when an exception is thrown:
1. A controller or service throws one of the handled exceptions.
2. Spring finds the matching method annotated with `@ExceptionHandler`.
3. The method creates an `ErrorResponse` object.
4. It returns that object wrapped in `ResponseEntity` with the right HTTP status.
5. The client receives JSON like:
   - `{ "error": "ACC-404", "message": "Account not found" }`
   - and status `404 NOT FOUND`.

### Mappings in your handler:
- `AccountNotFoundException` → `404 NOT FOUND`
- `AccountNotActiveException` → `403 FORBIDDEN`
- `InsufficientBalanceException` → `400 BAD REQUEST`
- `DuplicateTransferException` → `409 CONFLICT`
- `InvalidTransferException` → `422 UNPROCESSABLE ENTITY`

### Why this is useful:
- It centralizes error handling instead of scattering it across controllers.
- It keeps the API response format consistent.
- It prevents raw stack traces from reaching the client.
- It makes your REST API more predictable.

---

