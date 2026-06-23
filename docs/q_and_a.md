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

## Q9: What is a Bean in Spring?

### Answer:

A **bean** is any object that is created, managed, and controlled by the **Spring IoC (Inversion of Control) Container**. Instead of you writing `new AccountService()` yourself, you tell Spring "this class is a bean" — and Spring creates the object, wires its dependencies, and keeps it alive for the lifetime of the application.

Think of the Spring container as a **factory and registry** for objects. You register your classes with annotations, and the container does the rest.

### The Core Idea — Inversion of Control

Normally, if `TransferServiceImpl` needs an `AccountRepository`, you'd write:
```java
AccountRepository repo = new AccountRepository(); // YOU create it
TransferServiceImpl service = new TransferServiceImpl(repo); // YOU wire it
```

With Spring, you flip this around:
```java
@Service
public class TransferServiceImpl {
    @Autowired
    AccountRepository accountRepository; // SPRING creates and injects it for you
}
```

You **declare what you need** and Spring **provides it**. You never write `new AccountRepository()` anywhere. This is called **Dependency Injection (DI)**, and it's the heart of how Spring works.

### How Does Spring Know What's a Bean?

Spring scans your project at startup. Any class annotated with the following becomes a bean automatically:

| Annotation | What kind of bean |
|---|---|
| `@Component` | Generic bean — any purpose |
| `@Service` | Business logic layer bean |
| `@Repository` | Database access layer bean |
| `@Controller` / `@RestController` | HTTP request handling bean |
| `@Configuration` | Configuration class — defines other beans |

Methods inside a `@Configuration` class marked with `@Bean` also register beans:
```java
@Configuration
public class SpringSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // This object becomes a bean
    }
}
```
Spring calls this method once, stores the returned object, and from then on injects that same object wherever `BCryptPasswordEncoder` is needed.

### Real Example from This Project

In `UserServiceImpl.java`:
```java
@Service               ← tells Spring: "create one instance of this class as a bean"
public class UserServiceImpl implements UserService {

    @Autowired         ← tells Spring: "inject the UserRepository bean here"
    private UserRepository userRepository;

    @Autowired         ← inject AccountRepository bean
    private AccountRepository accountRepository;

    @Autowired         ← inject BCryptPasswordEncoder bean
    private BCryptPasswordEncoder passwordEncoder;
}
```

When the app starts:
1. Spring sees `@Service` → creates `UserServiceImpl` bean
2. Spring sees `@Autowired` on `userRepository` → finds the `UserRepository` bean (Spring Data JPA creates this automatically) → injects it
3. Same for `accountRepository` and `passwordEncoder`
4. `UserController` has `@Autowired private UserService userService;` → Spring injects the `UserServiceImpl` bean into the controller

### Key Properties of Beans

**Singleton by default:** Spring creates exactly **one instance** of each bean and reuses it everywhere. If 100 requests hit `UserController` simultaneously, they all use the same `UserServiceImpl` object. This is why you must not store user-specific data in service fields.

**Lifecycle management:** Spring can call special methods when the bean is created (`@PostConstruct`) or destroyed (`@PreDestroy`), though this project doesn't use those.

**Dependency graph:** Spring figures out the order to create beans. If `TransferServiceImpl` needs `AccountRepository` and `RewardService`, Spring creates those two first, then creates `TransferServiceImpl` and injects them.

### In Plain English

> A bean is just a Java object that Spring is in charge of. You put an annotation on your class, Spring creates it for you, keeps one copy of it, and hands it out to anyone who needs it. You never write `new` for a bean.

---

## Q10: What is a Component in Angular?

### Answer:

In Angular, a **component** is the **basic building block of the user interface**. Every visible piece of the screen — a login form, a dashboard card, a navigation bar — is a component.

Each component is responsible for one specific piece of the UI. This principle is called **separation of concerns**: each component has one job and does it well.

### Anatomy of a Component

Every Angular component is made of **three files** (sometimes four):

```
dashboard-component/
├── dashboard-component.ts      ← The brain (TypeScript class with logic)
├── dashboard-component.html    ← The face (HTML template — what the user sees)
├── dashboard-component.css     ← The skin (styles for this component only)
└── dashboard-component.spec.ts ← The tests (optional — unit tests)
```

**The `.ts` file** is the most important. It contains a TypeScript class decorated with `@Component`:
```typescript
@Component({
  selector: 'app-dashboard-component',   ← HTML tag name for this component
  templateUrl: './dashboard-component.html', ← Which HTML file to use
  styleUrl: './dashboard-component.css',     ← Which CSS file to use
  standalone: false,                         ← Belongs to a module (AppModule)
  changeDetection: ChangeDetectionStrategy.OnPush ← Performance strategy
})
export class DashboardComponent implements OnInit {
  // Properties (data)
  account = signal<Account>({...});

  // Lifecycle hook — runs once when component appears
  ngOnInit(): void {
    this.loadAccount();
  }

  // Methods (behaviour)
  loadAccount() { ... }
  logout() { ... }
}
```

### How Components Relate to Each Other

Angular apps are like a **tree of components**. The root is `App`, and every other component is nested inside it:

```
App (app.ts)
└── <router-outlet>
    ├── StartPageComponent  (at /start)
    ├── DashboardComponent  (at /dashboard)
    │   └── TopNavbarComponent (embedded inside)
    ├── TransferComponent   (at /transfer)
    ├── HistoryComponent    (at /history)
    ├── ProfileComponent    (at /profile)
    └── RewardComponent     (at /rewards)
```

`<router-outlet>` is the placeholder in `app.html` where Angular renders whichever component matches the current URL.

### Components in This Project

| Component | Route | What it shows |
|---|---|---|
| `StartPageComponent` | `/start` | Login & Register tabs |
| `DashboardComponent` | `/dashboard` | Account balance and overview |
| `TransferComponent` | `/transfer` | Send money form |
| `HistoryComponent` | `/history` | Transaction history table |
| `ProfileComponent` | `/profile` | Account details |
| `RewardComponent` | `/rewards` | Reward points history |
| `LoginComponent` | `/admin-login` | Legacy admin login |
| `TopNavbarComponent` | (inside others) | Navigation bar with logout |

### What Makes a Component Different from a Service?

| Component | Service |
|---|---|
| Has HTML, CSS, and is displayed on screen | No HTML — purely logic |
| Tied to a specific screen or UI element | Shared by many components |
| Created and destroyed as routes change | Usually lives for the whole app (singleton) |
| Injected with `@Component` | Injected with `@Injectable` |

### Lifecycle Hooks

Components have a lifecycle: they are **created → updated → destroyed**. Angular calls special methods at each stage:

| Hook | When it runs | Used in this project |
|---|---|---|
| `ngOnInit()` | Once, right after the component is created | Dashboard, History, Profile, Reward — to load data |
| `ngOnDestroy()` | When the user navigates away | TransferComponent — to unsubscribe & clear timers |

### The `@Component` Decorator — Key Properties

```typescript
@Component({
  selector: 'app-reward',            // The HTML tag: <app-reward></app-reward>
  imports: [CommonModule, DatePipe], // For standalone components only — declare dependencies
  templateUrl: './reward.html',      // External HTML file
  styleUrl: './reward.css',          // External CSS file
  changeDetection: ChangeDetectionStrategy.OnPush, // Performance mode
  standalone: true,                  // This component manages its own imports
})
```

**`selector`** — The custom HTML tag name. Angular replaces `<app-dashboard-component></app-dashboard-component>` in any parent template with the full rendered component.

**`standalone: true` vs `standalone: false`:**
- `false` — the component is declared in an `NgModule` (like `AppModule`). Most components in this project.
- `true` — the component manages its own dependencies via `imports`. `TransferComponent` and `RewardComponent` are standalone. Standalone is the newer, preferred Angular pattern.

**`changeDetection: ChangeDetectionStrategy.OnPush`** — A performance setting. Angular only re-renders this component when a Signal changes, an Observable emits, or you manually trigger it. Prevents unnecessary re-renders.

### In Plain English

> A component is a self-contained piece of your web page. It has its own HTML (what to show), CSS (how it looks), and TypeScript (what it does). Think of each screen or UI card as one component. Components talk to services to get data, and they talk to the router to navigate between pages.

---

## Q11: Annotations & Decorators — Complete Reference

This section covers **every annotation used in the backend (Java/Spring)** and every **decorator used in the frontend (Angular/TypeScript)**, found by scanning the entire project.

---

## Part A: Backend — Java / Spring Boot Annotations

Annotations in Java are labels starting with `@` that you place on classes, methods, or fields. They give Spring (or the Java compiler) extra instructions about how to treat that element. They do not change the logic of your code directly — they add metadata that frameworks read and act on.

---

### `@SpringBootApplication`
**Found in:** `MoneyTransferSystemApplication.java`

**What it does:** The single most important annotation in a Spring Boot project. It is a shortcut that combines three annotations in one:
- `@Configuration` — this class can define beans
- `@EnableAutoConfiguration` — auto-configure the app based on what's on the classpath (Spring sees `spring-boot-starter-web` → sets up a web server automatically)
- `@ComponentScan` — scan this package and all sub-packages for `@Component`, `@Service`, `@Repository`, `@Controller` classes and register them as beans

**Why we need it:** Without it, you'd have to manually start Tomcat, configure JPA, set up Security, and register every single class. `@SpringBootApplication` makes all of this happen automatically.

```java
@SpringBootApplication
public class MoneyTransferSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyTransferSystemApplication.class, args);
    }
}
```

---

### `@Configuration`
**Found in:** `SpringSecurityConfig.java`

**What it does:** Marks a class as a source of **bean definitions**. Methods inside this class annotated with `@Bean` will be called by Spring to create beans.

**Why we need it:** Spring needs to know which classes contain bean factory methods. Without `@Configuration`, `@Bean` methods won't be detected.

```java
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() { ... }
}
```

---

### `@EnableWebSecurity`
**Found in:** `SpringSecurityConfig.java`

**What it does:** Activates Spring Security for the application. Tells Spring to apply the security filter chain to all HTTP requests.

**Why we need it:** Without this, Spring Security would be on the classpath but completely inactive — all endpoints would be publicly accessible.

---

### `@Bean`
**Found in:** `SpringSecurityConfig.java` (on `passwordEncoder()`, `userDetailsService()`, `securityFilterChain()`, `corsConfigurationSource()`)

**What it does:** Placed on a method inside a `@Configuration` class. The method's return value becomes a Spring bean — Spring calls this method once, stores the result, and injects it wherever needed.

**Why we need it:** Some objects can't use `@Component` (e.g., third-party library classes like `BCryptPasswordEncoder` that you don't own the source code of). `@Bean` lets you register any object as a bean by writing a factory method.

```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    // Spring stores this instance. Anywhere you write
    // @Autowired BCryptPasswordEncoder encoder;
    // Spring injects this exact object.
}
```

---

### `@Service`
**Found in:** `UserServiceImpl.java`, `AccountServiceImpl.java`, `TransferServiceImpl.java`

**What it does:** Marks a class as a **service-layer bean**. Functionally equivalent to `@Component`, but communicates intent: "this class contains business logic."

**Why we need it:** Spring sees `@Service` during component scanning and registers the class as a singleton bean, making it available for injection everywhere.

```java
@Service
public class UserServiceImpl implements UserService { ... }
```

---

### `@Component`
**Found in:** `RewardService.java` — `@Component("RewardService")`

**What it does:** The generic stereotype annotation. Marks a class as a Spring-managed bean. `@Service`, `@Repository`, and `@Controller` are all specializations of `@Component`.

**Why `@Component` instead of `@Service` for `RewardService`?** The developer chose `@Component` with an explicit name `"RewardService"` to avoid ambiguity when Spring autowires it. Both work the same way for bean registration.

```java
@Component("RewardService")
public class RewardService implements RewardServiceInterface { ... }
```

---

### `@Autowired`
**Found in:** `UserServiceImpl`, `AccountServiceImpl`, `TransferServiceImpl`, `RewardService`, `AccountController`, `TransferController`, `UserController`

**What it does:** Tells Spring to **automatically inject** (provide) the matching bean into this field or constructor. Spring looks at the type of the field, finds a bean of that type, and assigns it.

**Why we need it:** Without `@Autowired`, the field would be `null` — you'd have to manually create and pass every dependency.

```java
@Autowired
private UserRepository userRepository;
// Spring finds the UserRepository bean (auto-created by Spring Data JPA)
// and assigns it to this field before the class is used.
```

**Note:** Modern Spring recommends constructor injection over field `@Autowired` for testability, but both work.

---

### `@Transactional`
**Found in:** `UserServiceImpl.register()`, `TransferServiceImpl.transfer()`, `RewardService.evaluateAndGrant()`

**What it does:** Wraps the method in a **database transaction**. All database operations inside the method succeed together or are all rolled back together if any exception is thrown.

**Why we need it:** Without `@Transactional` on `register()`, if the `User` save fails after the `Account` save, you'd end up with an `Account` in the database but no `User` linked to it — corrupted data.

```java
@Transactional
public String register(UserRegistrationRequest request) {
    accountRepository.save(account);  // Step 1
    userRepository.save(user);         // Step 2
    // If Step 2 throws, Step 1 is ROLLED BACK automatically.
}
```

---

### `@Override`
**Found in:** All service implementation classes (`UserServiceImpl`, `AccountServiceImpl`, `TransferServiceImpl`, `RewardService`)

**What it does:** This is a standard **Java annotation** (not Spring). It tells the compiler: "this method is supposed to override a method from a superclass or interface."

**Why we need it:** If you make a typo in the method signature, `@Override` causes a compile error instead of silently creating a new unrelated method. It's a safety check.

```java
@Override
public String register(UserRegistrationRequest request) {
    // Without @Override, if you misspelled "register" as "rgister",
    // Java would silently compile it as a new method, not implementing the interface.
    // With @Override, the compiler catches the error immediately.
}
```

---

### `@RestController`
**Found in:** `UserController`, `AccountController`, `TransferController`

**What it does:** Combines `@Controller` + `@ResponseBody`. Marks the class as a REST API controller where every method's return value is automatically serialized to JSON and written to the HTTP response body.

**Why we need it:** Without it, methods would try to return view names (like JSP templates) instead of JSON data.

```java
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController { ... }
```

---

### `@RequestMapping`
**Found in:** `UserController`, `AccountController`, `TransferController`

**What it does:** Sets the **base URL path** for all methods in a controller class. Every method's specific path is appended to this base.

```java
@RequestMapping("/api/v1/accounts")
// A method with @GetMapping("/{id}") becomes: GET /api/v1/accounts/{id}
```

---

### `@GetMapping`
**Found in:** `AccountController` (on `getAccountById`, `getAccountBalance`, `getTransactions`, `viewRewards`)

**What it does:** Maps HTTP **GET** requests to the annotated method. Shorthand for `@RequestMapping(method = RequestMethod.GET)`.

```java
@GetMapping("/{id}")
public ResponseEntity<AccountResponse> getAccountById(@PathVariable long id) { ... }
// Handles: GET /api/v1/accounts/5
```

---

### `@PostMapping`
**Found in:** `UserController` (register, login), `TransferController` (transfer)

**What it does:** Maps HTTP **POST** requests to the annotated method. POST is used when creating a resource or performing an action (like a transfer).

```java
@PostMapping
public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) { ... }
// Handles: POST /api/v1/transfers
```

---

### `@PathVariable`
**Found in:** `AccountController` (on `id` and `accountId` parameters)

**What it does:** Extracts a value from the URL path and binds it to the method parameter.

```java
@GetMapping("/{id}")
public ResponseEntity<AccountResponse> getAccountById(@PathVariable long id) { ... }
// URL: /api/v1/accounts/5 → id = 5
```

---

### `@RequestBody`
**Found in:** `UserController.register()`, `UserController.login()`, `TransferController.transfer()`

**What it does:** Tells Spring to deserialize (convert) the incoming HTTP request body (JSON) into a Java object.

```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
    // Spring reads the JSON body:
    // { "email": "ann@example.com", "password": "secret" }
    // and creates a UserLoginRequest object from it.
}
```

---

### `@Valid`
**Found in:** `UserController.register()`, `UserController.login()`, `TransferController.transfer()`

**What it does:** Triggers **Bean Validation** on the annotated parameter. Spring reads the validation annotations on the DTO's fields (`@NotBlank`, `@Email`, `@Size`, etc.) and validates them before the method body runs. If any check fails, Spring returns a `400 Bad Request` automatically — the method is never called.

```java
public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
    // @Valid checks: name not blank, email is valid, password >= 6 chars
    // Only reaches this line if ALL checks pass.
}
```

---

### `@CrossOrigin`
**Found in:** `AccountController`, `TransferController`

**What it does:** Configures **CORS (Cross-Origin Resource Sharing)** for the controller. Allows browsers to make requests from a different origin (different domain or port).

```java
@CrossOrigin(origins = "*")
// Allows requests from ANY origin.
// Note: The global CORS config in SpringSecurityConfig already handles this.
// This annotation is somewhat redundant here but harmless.
```

---

### `@ControllerAdvice`
**Found in:** `GlobalExceptionHandler.java`

**What it does:** Marks a class as a **global exception handler** that applies to all controllers in the application. Without it, exception handlers would only apply to one specific controller.

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // Exception handlers here apply to ALL controllers in the app
}
```

---

### `@ExceptionHandler`
**Found in:** `GlobalExceptionHandler.java` (on every `handle*Exception` method)

**What it does:** Tells Spring: "when this specific exception type is thrown anywhere in a controller or service, call this method to handle it."

```java
@ExceptionHandler(InsufficientBalanceException.class)
public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException e) {
    return new ResponseEntity<>(new ErrorResponse("TRX-400", e.getMessage()), HttpStatus.BAD_REQUEST);
}
```

---

### `@Entity`
**Found in:** `User.java`, `Account.java`, `TransactionLog.java`, `RewardLedger.java`

**What it does:** Tells JPA (Hibernate) that this Java class maps to a **database table**. Hibernate will read this class at startup and create/update the corresponding table.

```java
@Entity
public class Account { ... }
// Creates/maps to the "accounts" table in MySQL
```

---

### `@Table`
**Found in:** `User.java` → `@Table(name = "users")`, `Account.java` → `@Table(name = "accounts")`, `TransactionLog.java` → `@Table(name = "transaction_logs")`, `RewardLedger.java` → `@Table(name = "reward_ledger")`

**What it does:** Specifies the **exact name of the database table** this entity maps to. Without it, JPA uses the class name (so `TransactionLog` would map to `TRANSACTIONLOG`).

---

### `@Id`
**Found in:** All entity classes (`User`, `Account`, `TransactionLog`, `RewardLedger`)

**What it does:** Marks the field as the **primary key** of the database table. Every JPA entity must have exactly one `@Id` field.

---

### `@GeneratedValue`
**Found in:** All entity classes

**What it does:** Specifies how the primary key value is generated.

| Strategy | How it works | Used in |
|---|---|---|
| `GenerationType.IDENTITY` | MySQL auto-increment | `User`, `Account`, `RewardLedger` |
| `GenerationType.UUID` | JPA generates a random UUID | `TransactionLog` |

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
// MySQL generates: 1, 2, 3, 4... automatically on each INSERT

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
// JPA generates: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
```

---

### `@Column`
**Found in:** All entity classes

**What it does:** Maps a Java field to a specific **database column**. Lets you customise the column name, set constraints (nullable, unique), and configure the data type.

```java
@Column(name = "email", nullable = false, unique = true, length = 100)
private String email;
// Database column: "email" VARCHAR(100) NOT NULL UNIQUE

@Column(name = "balance", precision = 18)
private BigDecimal balance;
// Database column: "balance" DECIMAL(18)

@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;
// updatable = false: once written, this column is never updated by Hibernate
```

---

### `@NotNull`
**Found in:** Entity classes (`User`, `Account`, `TransactionLog`), DTO `TransferRequest`

**What it does:** Bean Validation annotation. The annotated field **must not be null**. Works with `@Valid` in controllers.

**Two contexts where it's used:**
1. On **entity fields** — acts as a documentation hint and can be picked up by some validators
2. On **DTO fields** — actively enforced when `@Valid` is used in a controller

---

### `@Email`
**Found in:** `User.java`

**What it does:** Validates that the field value is a **syntactically valid email address** (contains `@` and a domain). Used on the entity field as documentation and enforced on the DTO.

---

### `@NotBlank`
**Found in:** `UserRegistrationRequest.java`, `UserLoginRequest.java`

**What it does:** The field must not be null, empty (`""`), or only whitespace (`"   "`). Stricter than `@NotNull` — catches the case where someone submits a form with just spaces.

```java
@NotBlank(message = "Email is required")
private String email;
```

The `message` is shown in the validation error response.

---

### `@Size`
**Found in:** `UserRegistrationRequest.java`

**What it does:** Validates the **length** of a String (or size of a collection).

```java
@Size(min = 6, message = "Password must be at least 6 characters")
private String password;
```

---

### `@DecimalMin`
**Found in:** `TransferRequest.java`

**What it does:** Validates that a numeric value is **at least** the specified minimum.

```java
@NotNull @DecimalMin("0.01") BigDecimal amount
// Ensures amount >= 0.01 — prevents zero or negative transfers
```

---

### `@Enumerated`
**Found in:** `Account.java` (on `status`), `TransactionLog.java` (on `status`)

**What it does:** Tells Hibernate how to store an enum value in the database.

```java
@Enumerated(EnumType.STRING)
@Column(name = "status")
private AccountStatus status;
// Stores "ACTIVE", "LOCKED", or "CLOSED" as text in the DB
// (Not as integers 0, 1, 2 — which would be unreadable in the DB)
```

`EnumType.STRING` is always preferred over `EnumType.ORDINAL` because:
- The database is human-readable
- Adding new enum values in the middle won't corrupt existing data

---

### `@Version`
**Found in:** `Account.java`

**What it does:** Marks the field as the **optimistic locking version column**. Hibernate automatically manages this field — incrementing it on every update.

**How it prevents data corruption:**
```java
@Version
private int version = 0;
```
When Hibernate saves an Account, it generates:
```sql
UPDATE accounts SET balance = 1000, version = 3
WHERE id = 5 AND version = 2  ← must match current version
```
If two threads try to update the same account at the same time, one will update successfully (version 2 → 3), and the second will fail because it still has `version = 2` but the DB now has `3`. Hibernate throws `OptimisticLockException`. This prevents the "lost update" problem without locking the database row.

---

### `@PrePersist`
**Found in:** `User.java` (on `onCreate()` method)

**What it does:** A **JPA lifecycle callback**. The annotated method is called automatically by Hibernate **just before** the entity is first saved (INSERT) to the database.

```java
@PrePersist
protected void onCreate() {
    if (this.createdAt == null) {
        this.createdAt = LocalDateTime.now();
    }
}
// createdAt is set automatically at save time.
// You never need to manually call this.
```

---

### `@Setter` and `@Getter` (Lombok)
**Found in:** `TransactionLog.java`

**What they do:** Lombok annotations that **auto-generate** getter and setter methods at compile time.

```java
@Setter
@Getter
public class TransactionLog {
    private UUID id;
    private BigDecimal amount;
    // Lombok generates:
    // public UUID getId() { return id; }
    // public void setId(UUID id) { this.id = id; }
    // public BigDecimal getAmount() { return amount; }
    // public void setAmount(BigDecimal amount) { this.amount = amount; }
    // ... for every field
}
```

Without Lombok, you'd write hundreds of lines of repetitive getter/setter methods manually.

---

### `@NoArgsConstructor` and `@AllArgsConstructor` (Lombok)
**Found in:** `RewardLedger.java`

**What they do:**
- `@NoArgsConstructor` — generates `public RewardLedger() {}` (empty constructor). JPA **requires** a no-argument constructor for all entities.
- `@AllArgsConstructor` — generates a constructor with all fields as parameters: `public RewardLedger(Long id, Long accountId, UUID transactionId, int pointsAwarded, String description, LocalDateTime createdAt) {}`

---

### `@Query`
**Found in:** `RewardLedgerRepository.java`

**What it does:** Lets you write a **custom JPQL (Java Persistence Query Language) query** when Spring's automatic method name derivation isn't powerful enough.

```java
@Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardLedger r WHERE r.accountId = :accountId")
int sumPointsByAccountId(@Param("accountId") Long accountId);
```

`JPQL` looks like SQL but uses class and field names (not table and column names):
- `RewardLedger` → the Java class (maps to `reward_ledger` table)
- `r.pointsAwarded` → the Java field (maps to `points_awarded` column)
- `COALESCE(..., 0)` → returns 0 if no rows found (prevents NULL being returned)

---

### `@Param`
**Found in:** `RewardLedgerRepository.java`

**What it does:** Names a method parameter so it can be referenced in a `@Query` with `:paramName` syntax.

```java
int sumPointsByAccountId(@Param("accountId") Long accountId);
// The query uses :accountId, which Spring replaces with the method parameter value.
```

---

### `@SuppressWarnings`
**Found in:** `InsufficientBalanceException.java` — `@SuppressWarnings("serial")`

**What it does:** A standard Java annotation that tells the compiler to suppress a specific warning. `"serial"` suppresses the warning about `serialVersionUID` not being declared in a `Serializable` class. This is a minor housekeeping annotation.

---

## Part B: Frontend — Angular / TypeScript Decorators

In TypeScript (Angular), decorators serve the same purpose as annotations in Java — they add metadata to classes, properties, and methods that the Angular framework reads and acts on. Decorators always start with `@`.

---

### `@NgModule`
**Found in:** `app-module.ts`, `app-routing-module.ts`, `app.module.server.ts`

**What it does:** Defines an **Angular Module** — a container that groups related components, services, and other modules. The root module (`AppModule`) is what Angular bootstraps first.

```typescript
@NgModule({
  declarations: [...],  // Components, directives, pipes that BELONG to this module
  imports: [...],       // Other modules whose features are needed
  providers: [...],     // Services and config (dependency injection)
  bootstrap: [App],     // The root component Angular renders first
})
export class AppModule {}
```

---

### `@Component`
**Found in:** All component files (`dashboard-component.ts`, `login-component.ts`, `transfer-component.ts`, `history-component.ts`, `profile-component.ts`, `reward-component.ts`, `start-page-component.ts`, `top-navbar-component.ts`, `app.ts`)

**What it does:** The most important Angular decorator. Marks a TypeScript class as a **UI component** and attaches metadata about how to render it.

```typescript
@Component({
  selector: 'app-dashboard-component', // Custom HTML tag name
  templateUrl: './dashboard-component.html', // The HTML template file
  styleUrl: './dashboard-component.css',     // The CSS file
  standalone: false,                         // Declared in AppModule
  changeDetection: ChangeDetectionStrategy.OnPush // Performance strategy
})
export class DashboardComponent { ... }
```

**Key properties explained:**

| Property | What it does |
|---|---|
| `selector` | The HTML tag name (`<app-dashboard-component>`) used to embed this component |
| `templateUrl` | Path to the HTML template file (use `template:` for inline HTML) |
| `styleUrl` | Path to the CSS file (use `styles:` for inline CSS) |
| `standalone: true` | Component manages its own `imports` — doesn't need to be declared in a module |
| `standalone: false` | Component must be declared in an `NgModule` (the traditional pattern) |
| `imports` | (Standalone only) List of modules/pipes/directives this component needs |
| `changeDetection` | `Default` = check on every event, `OnPush` = only check when signals/inputs change |

---

### `@Injectable`
**Found in:** All service files (`auth-service.ts`, `user-service.ts`, `account-service.ts`, `transfer-service.ts`, `reward-service.ts`), `auth-interceptor.ts`, `auth-guard.ts`

**What it does:** Marks a class as **injectable** — it can be provided to (injected into) other classes via Angular's Dependency Injection system. Without `@Injectable`, Angular cannot inject the class.

```typescript
@Injectable({
  providedIn: 'root'  // ← Create ONE instance for the entire app (singleton)
})
export class AuthService { ... }
```

**`providedIn: 'root'`** means Angular creates exactly one instance of this service and shares it with every component and service that requests it. This is equivalent to `@Service` in Spring.

**`@Injectable()` without `providedIn`** (used on `AuthInterceptor`):
```typescript
@Injectable()
export class AuthInterceptor { ... }
```
Here, `AuthInterceptor` is registered manually in `app-module.ts` via the `providers` array, so it doesn't need `providedIn: 'root'`.

---

### `@Inject`
**Found in:** `auth-service.ts`

**What it does:** Injects a **token-based dependency** — a dependency that isn't identified by its class type but by a special token.

```typescript
constructor(
  private router: Router,
  @Inject(PLATFORM_ID) platformId: Object, // ← @Inject needed here
) {
  this.isBrowser = isPlatformBrowser(platformId);
}
```

`PLATFORM_ID` is an Angular injection token (not a class), so Angular doesn't know to inject it automatically by type — you have to use `@Inject` to specify the token explicitly. `isPlatformBrowser(platformId)` returns `true` when running in a browser and `false` when running on a Node.js server (SSR), so `localStorage` calls are only made in the browser.

---

### Summary Tables

**Backend Annotations Quick Reference:**

| Annotation | Category | One-line explanation |
|---|---|---|
| `@SpringBootApplication` | Boot | Start the app, enable auto-config, scan components |
| `@Configuration` | Spring Core | This class defines beans via `@Bean` methods |
| `@EnableWebSecurity` | Security | Activate Spring Security |
| `@Bean` | Spring Core | This method's return value becomes a Spring bean |
| `@Service` | Stereotype | This is a business-logic bean |
| `@Component` | Stereotype | Generic Spring-managed bean |
| `@Autowired` | DI | Inject the matching bean into this field |
| `@Transactional` | Transaction | Wrap in DB transaction; rollback on exception |
| `@Override` | Java | Confirms this method implements an interface/parent method |
| `@RestController` | Web | REST controller — returns JSON responses |
| `@RequestMapping` | Web | Set the base URL path for the controller |
| `@GetMapping` | Web | Handle HTTP GET requests |
| `@PostMapping` | Web | Handle HTTP POST requests |
| `@PathVariable` | Web | Bind URL path segment to a method parameter |
| `@RequestBody` | Web | Deserialize JSON request body into Java object |
| `@Valid` | Validation | Run Bean Validation on the annotated parameter |
| `@CrossOrigin` | CORS | Allow cross-origin browser requests |
| `@ControllerAdvice` | Exception | Global exception handler (applies to all controllers) |
| `@ExceptionHandler` | Exception | Handle a specific exception type |
| `@Entity` | JPA | This class maps to a database table |
| `@Table` | JPA | Set the database table name |
| `@Id` | JPA | This field is the primary key |
| `@GeneratedValue` | JPA | Auto-generate the primary key (identity or UUID) |
| `@Column` | JPA | Map field to a column; set name, constraints |
| `@NotNull` | Validation | Field must not be null |
| `@Email` | Validation | Field must be a valid email address |
| `@NotBlank` | Validation | Field must not be null or whitespace |
| `@Size` | Validation | Field must have a specific length/size |
| `@DecimalMin` | Validation | Numeric field must be >= minimum value |
| `@Enumerated` | JPA | Store enum as STRING (not integer) in DB |
| `@Version` | JPA | Optimistic locking — prevents concurrent update conflicts |
| `@PrePersist` | JPA | Run this method before first INSERT to DB |
| `@Setter` / `@Getter` | Lombok | Auto-generate setters/getters |
| `@NoArgsConstructor` | Lombok | Auto-generate empty constructor |
| `@AllArgsConstructor` | Lombok | Auto-generate all-args constructor |
| `@Query` | Spring Data | Write custom JPQL for complex queries |
| `@Param` | Spring Data | Name a parameter for use in `@Query` |
| `@SuppressWarnings` | Java | Suppress a specific compiler warning |

**Frontend Decorators Quick Reference:**

| Decorator | Where Used | One-line explanation |
|---|---|---|
| `@NgModule` | `app-module.ts` | Define an Angular module (groups components, services) |
| `@Component` | All component `.ts` files | Mark a class as a UI component with template & styles |
| `@Injectable` | All service files, interceptor, guard | Make a class injectable via Angular DI |
| `@Inject` | `auth-service.ts` | Inject a token-based dependency (not class-based) |

