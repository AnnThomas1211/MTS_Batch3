# Backend Documentation — Money Transfer System

> **Audience:** This document is written for beginner full-stack developers. Every file in the backend is explained in detail — what it does, why it exists, when it is used, and how it connects to the rest of the system. Read it top-to-bottom for the first time; after that, use it as a reference when you want to understand a specific part.

---

## What Is the Backend?

The backend is the **server-side** part of the application. The frontend (Angular) is what the user sees in their browser. The backend is the "brain" that runs on a server, holds the business logic, talks to the database, and sends data back to the frontend.

In this project, the backend is built with **Spring Boot**, a popular Java framework that makes it very easy to build REST APIs (web services that accept HTTP requests and return JSON responses).

### Technology Stack
| Technology | Role |
|---|---|
| **Java 17** | Programming language |
| **Spring Boot 3.0.13** | Application framework — handles server startup, dependency injection, web layer |
| **Spring Data JPA + Hibernate** | ORM layer — converts Java objects into database rows and back |
| **MySQL 8** | Relational database — where all data is permanently stored |
| **Spring Security** | Security framework — controls who is allowed to access which endpoints |
| **Lombok** | Code generator — reduces boilerplate (getters, setters, constructors) |
| **Maven** | Build tool — downloads dependencies, compiles and packages the app |

---

## Project Directory Structure

```
backend/MoneyTransferSystem/
├── pom.xml                            ← Maven build file (dependencies)
├── mvnw / mvnw.cmd                    ← Maven wrapper scripts (run without installing Maven)
└── src/
    └── main/
        ├── java/com/fidelity/mts/
        │   ├── MoneyTransferSystemApplication.java   ← Application entry point
        │   ├── application/dto/                       ← Data Transfer Objects (DTOs)
        │   ├── controller/                            ← REST Controllers (HTTP layer)
        │   ├── domain/
        │   │   ├── enums/                             ← Shared enumerations
        │   │   ├── exception/                         ← Custom exceptions + error handler
        │   │   └── model/                             ← JPA Entities (database tables)
        │   ├── repo/                                  ← Repositories (database queries)
        │   ├── security/                              ← Spring Security configuration
        │   └── service/                               ← Business logic layer
        └── resources/
            └── application.properties                 ← App configuration (DB URL, credentials, etc.)
```

---

## The Layered Architecture

Before diving into individual files, understand the **layers** of the backend. Every request from the frontend passes through these layers in order:

```
Frontend (Angular)
      ↓  HTTP Request
  [Controller]         ← Receives the HTTP request, calls the service
      ↓
  [Service]            ← Contains the business logic (rules, validations)
      ↓
  [Repository]         ← Speaks to the database (SELECT, INSERT, UPDATE)
      ↓
  [Database (MySQL)]   ← Where data actually lives
      ↑  Returns data back up through the same layers
```

This separation is intentional and is called **Separation of Concerns**. Each layer has one job and doesn't need to know how the other layers work internally.

---

## File-by-File Explanation

---

### `pom.xml` — Maven Project Configuration

**What is it?**
`pom.xml` stands for **Project Object Model**. It is the configuration file for Maven, the build tool. It is like a recipe that tells Maven:
- What the project is called
- What external libraries (dependencies) it needs
- What Java version to use

**Why do we need it?**
Without `pom.xml`, Maven wouldn't know what to download or how to compile the project. Think of it like a shopping list — Maven reads the list, downloads everything from the internet (Maven Central repository), and adds it to your project.

**Key dependencies declared in this file:**
| Dependency | What it does |
|---|---|
| `spring-boot-starter-web` | Adds the HTTP server (Tomcat) and REST support |
| `spring-boot-starter-data-jpa` | Adds Hibernate ORM so Java objects map to database tables |
| `spring-boot-starter-security` | Adds Spring Security (authentication/authorization) |
| `spring-boot-starter-validation` | Adds `@NotNull`, `@Email`, `@Size` validation annotations |
| `spring-boot-starter-aop` | Adds Aspect-Oriented Programming (used internally) |
| `springdoc-openapi-starter-webmvc-ui` | Auto-generates a Swagger UI at `/swagger-ui.html` |
| `mysql-connector-java` | JDBC driver that lets Java talk to MySQL |
| `lombok` | Auto-generates getters, setters, constructors via annotations |
| `spring-boot-starter-test` | Testing framework (JUnit, Mockito) |

**When is it used?**
Maven reads `pom.xml` when you run `./mvnw spring-boot:run` or `./mvnw package`. It is not used at runtime — only during build and dependency resolution.

---

### `application.properties` — Application Configuration

**Location:** `src/main/resources/application.properties`

**What is it?**
This file holds all the **environment-specific configuration** for the application. Instead of hardcoding values like database URLs inside Java code, they are placed here so they can easily be changed.

**Contents explained line by line:**
```properties
spring.application.name=MoneyTransferSystem
# The name of the application. Used in logs and monitoring.

spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
# Tells Spring which JDBC driver to use to connect to the database.
# MySQL requires this specific driver class.

spring.datasource.url=jdbc:mysql://localhost:3306/mts_test
# The connection string for the database.
# localhost:3306 — MySQL is running on the same machine, port 3306 (MySQL's default).
# mts_test — the name of the database (schema) to use.

spring.datasource.username=root
spring.datasource.password=Cloud@123$
# The MySQL login credentials. 
# NEVER commit real passwords to a public repository in production.

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
# Tells Hibernate which "dialect" (SQL variant) to generate.
# Different databases (MySQL, PostgreSQL, Oracle) have slightly different SQL syntax.

spring.jpa.hibernate.ddl-auto=update
# Controls how Hibernate manages the database schema on startup.
# "update" = Hibernate reads the @Entity classes and automatically
#             creates or modifies the tables to match. 
# Good for development. For production, use "validate" and manage 
# schema changes with a migration tool like Flyway.

spring.jpa.show-sql=true
# Prints every SQL query that Hibernate generates to the console.
# Very useful for debugging — you can see exactly what queries are running.

spring.security.user.name=admin
spring.security.user.password=1234
# Default in-memory Spring Security user. 
# NOTE: This is overridden in SpringSecurityConfig.java where the password
# is re-encoded with BCrypt. The plain text here is just a reference.
```

**Why does this file matter?**
If you change your MySQL password or run the database on a different port, you only need to change this file — not any Java code.

---

### `MoneyTransferSystemApplication.java` — Application Entry Point

**Location:** `src/main/java/com/fidelity/mts/`

**What is it?**
This is the **starting point** of the entire application. It contains the `main()` method, which is the first method Java executes when you run the program.

```java
@SpringBootApplication
public class MoneyTransferSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyTransferSystemApplication.class, args);
    }
}
```

**Breaking it down:**
- `@SpringBootApplication` is a shortcut annotation that does three things:
  1. `@Configuration` — this class can define Spring beans
  2. `@EnableAutoConfiguration` — Spring Boot automatically configures things like web server, JPA, Security based on what's on the classpath
  3. `@ComponentScan` — Spring automatically discovers all `@Component`, `@Service`, `@Repository`, `@Controller` classes in this package and sub-packages
- `SpringApplication.run(...)` — this starts the embedded Tomcat web server and loads the entire Spring application context

**When is it used?**
Every single time you start the backend server. This is the "on switch" for the application.

**Analogy:** Think of this like the `index.js` in a Node.js app or `main.py` in a Python app. It's the very first thing that runs.

---

## Domain Layer — Data Models

The domain layer contains the **core data** of the application. These are Java classes annotated with `@Entity` that map directly to MySQL database tables. When Spring Boot starts, Hibernate reads these classes and creates/updates the tables automatically.

---

### `domain/model/User.java` — User Entity

**What is it?**
Represents a **registered user** of the application. Maps to the `users` table in MySQL.

**Fields:**
| Field | Database Column | Purpose |
|---|---|---|
| `id` | `id` (PK, AUTO_INCREMENT) | Unique identifier for the user |
| `name` | `name` | The user's full name |
| `email` | `email` (UNIQUE) | Used as login identifier. Must be unique — two users can't share an email. |
| `passwordHash` | `password_hash` | The **hashed** (encrypted) version of the password. **Never store plain text passwords.** |
| `accountId` | `account_id` (UNIQUE) | Foreign key linking to the `accounts` table |
| `createdAt` | `created_at` | Timestamp of when the user registered |

**Why is the password hashed?**
If the database is ever breached, attackers see meaningless hash strings, not real passwords. The app uses **BCrypt**, a one-way hashing algorithm. You can verify a password against a hash but you cannot reverse the hash to get the original password.

**Important annotations:**
- `@Entity` — tells JPA this is a database table
- `@Table(name = "users")` — maps to the `users` table
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` — `id` is the primary key and is auto-incremented by MySQL
- `@Column(unique = true)` on `email` — adds a UNIQUE constraint in the database
- `@PrePersist` on `onCreate()` — this method runs automatically before the entity is first saved to set `createdAt`

**Relationship with Account:**
Each User has exactly one Account (stored as `accountId`). This is a one-to-one relationship. The User doesn't hold the Account object directly — it holds the `accountId` as a Long. This is called a **foreign key reference**.

**When is it used?**
- During **registration**: `UserServiceImpl.register()` creates a new `User` and saves it
- During **login**: `UserServiceImpl.login()` loads the User by email and checks the password

---

### `domain/model/Account.java` — Account Entity

**What is it?**
Represents a **bank account**. Maps to the `accounts` table in MySQL. This is the central object in the system — most operations revolve around accounts.

**Fields:**
| Field | Database Column | Purpose |
|---|---|---|
| `id` | `id` (PK) | Unique account number |
| `holderName` | `holder_name` | Name displayed in the UI |
| `balance` | `balance` (DECIMAL) | Current account balance. Uses `BigDecimal` for precision — not `double` or `float`, which can have rounding errors |
| `status` | `status` (ENUM) | `ACTIVE`, `LOCKED`, or `CLOSED` |
| `version` | `version` | Used for **optimistic locking** (explained below) |
| `lastUpdated` | `last_updated` | Timestamp of last balance change |

**Business methods (not just getters/setters):**
The `Account` class has actual business logic embedded in it — this is called a **Rich Domain Model**:

```java
public void debit(BigDecimal amount) {
    // 1. Check account is ACTIVE
    if (!this.isActive()) throw new AccountNotActiveException(this.id);
    // 2. Check sufficient balance
    if (this.balance.compareTo(amount) < 0) 
        throw new InsufficientBalanceException(this.id, amount, balance);
    // 3. Subtract
    this.balance = this.balance.subtract(amount);
    this.lastUpdated = LocalDateTime.now();
}

public void credit(BigDecimal amount) {
    // 1. Check account is ACTIVE
    if (!this.isActive()) throw new AccountNotActiveException(this.id);
    // 2. Add
    this.balance = this.balance.add(amount);
    this.lastUpdated = LocalDateTime.now();
}
```

**Why put logic inside the entity?**
By putting validation inside the entity, the business rules (like "you can't debit a closed account") are enforced no matter where the entity is used. The service layer doesn't have to remember to check all these conditions — the entity does it for them.

**What is Optimistic Locking (`@Version`)?**
```java
@Version
private int version = 0;
```
Imagine two users simultaneously try to withdraw from the same account. Without protection, both could read the same balance, both subtract money, and the database ends up in an incorrect state (this is called a **race condition**).

`@Version` tells Hibernate to add a `WHERE version = ?` clause to all UPDATE statements. If two threads try to save the same row simultaneously, one will succeed and increment the version number. The second thread will try to update with the old version number, fail, and throw an `OptimisticLockException`. This is safe and prevents data corruption without requiring database-level locks.

**When is it used?**
- During **registration**: A new Account is created with balance 0
- During **transfers**: Both `fromAccount` and `toAccount` are loaded, modified via `debit()`/`credit()`, and saved
- During **dashboard load**: The account is fetched and displayed to the user

---

### `domain/model/TransactionLog.java` — Transaction Log Entity

**What is it?**
A **permanent record of every transfer attempt** — both successful and failed ones. Maps to the `transaction_logs` table in MySQL.

**This is one of the most important entities** — it is the system's audit trail.

**Fields:**
| Field | Database Column | Purpose |
|---|---|---|
| `id` | `id` (UUID, PK) | Unique ID for this transaction. Uses UUID (not auto-increment) for security |
| `fromAccountId` | `from_account` | Sender's account ID |
| `toAccountId` | `to_account` | Recipient's account ID |
| `fromAccountName` | `from_account_name` | Sender's name at time of transfer (denormalized for history readability) |
| `toAccountName` | `to_account_name` | Recipient's name at time of transfer |
| `amount` | `amount` | Transfer amount |
| `status` | `status` | `SUCCESS` or `FAILED` |
| `failureReason` | `failure_reason` | If FAILED, why it failed (e.g., "Insufficient balance") |
| `idempotencyKey` | `idempotency_key` (UNIQUE) | UUID sent by the client. Prevents duplicate transfers. |
| `createdOn` | `created_on` | When the transfer was attempted |

**Why UUID for the primary key instead of a simple number?**
UUIDs are random 128-bit values. They are virtually impossible to guess. If the transaction ID were `1, 2, 3...`, a malicious user could easily guess transaction IDs. UUIDs (`550e8400-e29b-41d4-a716-446655440000`) prevent this.

**What is an idempotency key?**
This is a concept from financial systems. If a user clicks "Transfer" and the internet drops before they get a response, did the transfer go through or not? They might click again and accidentally transfer money twice.

The **idempotency key** (a UUID generated by the frontend before sending the request) solves this. The backend checks: "Have I seen this key before?" If yes, it rejects the duplicate. If no, it processes the transfer and saves the key. Even if the client sends the same request 5 times with the same key, only one transfer will happen.

**Why store `failureReason`?**
This is how the History page shows users why their transfer failed. The string message from the exception (e.g., `"Insufficient balance in account 42. Attempted: 500, Available: 100"`) is saved here.

**Annotations from Lombok:**
```java
@Setter
@Getter
```
Lombok automatically generates all the `getId()`, `setId()`, `getAmount()`, etc. methods at compile time. Without Lombok, you'd have to write hundreds of lines of boilerplate.

---

### `domain/model/RewardLedger.java` — Reward Ledger Entity

**What is it?**
Each row is one **reward points event** — whenever a user earns points for a transfer. Maps to the `reward_ledger` table.

**Fields:**
| Field | Purpose |
|---|---|
| `id` | Auto-increment primary key |
| `accountId` | Which account earned the points |
| `transactionId` | The UUID of the transfer that triggered the reward |
| `pointsAwarded` | How many points were awarded (e.g., 5) |
| `description` | Human-readable description (e.g., "Earned 5 point(s) for transferring ₹500 to account #3") |
| `createdAt` | When the reward was given |

**Lombok annotations used here:**
```java
@Getter
@Setter
@NoArgsConstructor   ← Generates: public RewardLedger() {}
@AllArgsConstructor  ← Generates: public RewardLedger(Long id, Long accountId, ...) {}
```
These save a lot of repetitive code.

---

### `domain/enums/Enums.java` — Shared Enumerations

**What is it?**
A single file containing all the **enum types** (fixed sets of values) used across the application.

```java
public class Enums {
    public enum AccountStatus { ACTIVE, LOCKED, CLOSED }
    public enum TransactionStatus { SUCCESS, FAILED }
}
```

**Why use enums instead of plain strings?**
If you used strings like `"active"`, `"ACTIVE"`, `"Active"` — a typo could cause bugs that are very hard to find. Enums make it a compile-time error to use an invalid value. The compiler catches the mistake before you even run the code.

**How are they stored in the database?**
The `@Enumerated(EnumType.STRING)` annotation on the entity fields tells Hibernate to store the enum as its string name (e.g., `"ACTIVE"`) rather than an integer (0, 1, 2). This makes the database human-readable.

---

## Exception Layer — Custom Errors

The exception layer defines what happens when something goes wrong. Instead of returning generic error messages, each specific error condition has its own exception class.

---

### `domain/exception/GlobalExceptionHandler.java` — Central Error Handler

**What is it?**
This is the **traffic cop for all errors** in the application. It is annotated with `@ControllerAdvice`, which means Spring automatically routes any unhandled exception here before sending a response to the client.

**Why do we need this?**
Without this, if `AccountNotFoundException` is thrown inside `AccountServiceImpl`, Spring Boot would return a generic `500 Internal Server Error` response with an ugly Java stack trace. That's terrible UX and also a security risk (stack traces reveal implementation details).

With `GlobalExceptionHandler`, each specific exception maps to a specific HTTP status code and a clean JSON error response:

```java
@ExceptionHandler(AccountNotFoundException.class)
public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException e) {
    ErrorResponse res = new ErrorResponse("ACC-404", e.getMessage());
    return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);  // HTTP 404
}
```

**Error code mapping:**
| Exception | Error Code | HTTP Status | Meaning |
|---|---|---|---|
| `AccountNotFoundException` | `ACC-404` | 404 Not Found | The account ID doesn't exist |
| `AccountNotActiveException` | `ACC-403` | 403 Forbidden | Account is LOCKED or CLOSED |
| `InsufficientBalanceException` | `TRX-400` | 400 Bad Request | Not enough money |
| `DuplicateTransferException` | `TRX-409` | 409 Conflict | Same idempotency key was used before |
| `InvalidTransferException` | `VAL-422` | 422 Unprocessable Entity | Transfer to yourself or amount ≤ 0 |
| `DuplicateEmailException` | `USR-409` | 409 Conflict | Email already registered |
| `InvalidCredentialsException` | `USR-401` | 401 Unauthorized | Wrong email or password |

**When is it used?**
Every time an exception is thrown anywhere in the application, Spring automatically routes it to the matching `@ExceptionHandler` method here.

---

### Custom Exception Classes

Each of these is a simple class that extends `RuntimeException`. They all have one purpose: to carry a meaningful error message from the point of failure to the `GlobalExceptionHandler`.

#### `AccountNotFoundException.java`
**When thrown:** In `AccountServiceImpl` or `TransferServiceImpl` when `accountRepository.findById(id)` returns empty — meaning the provided ID doesn't exist in the database.

```java
// Example message: "Account with ID 99 not found."
```

#### `AccountNotActiveException.java`
**When thrown:** In `Account.debit()` and `Account.credit()`, and in `TransferServiceImpl.executeTransfer()`, when `account.isActive()` returns false — i.e., the account is LOCKED or CLOSED.

#### `InsufficientBalanceException.java`
**When thrown:** In `Account.debit()` when the transfer amount exceeds the current balance.
```java
// Message: "Insufficient balance in account 42. Attempted: 500, Available: 100"
```
This is very informative — tells both the attempted amount and the current balance.

#### `DuplicateTransferException.java`
**When thrown:** In `TransferServiceImpl.checkIdempotency()` when the same `idempotencyKey` UUID is found in the `transaction_logs` table. This prevents users from accidentally transferring money twice.

#### `InvalidTransferException.java`
**When thrown:** In `TransferServiceImpl.validateTransfer()` when:
- `fromAccountId == toAccountId` (cannot transfer to yourself)
- `amount <= 0` (cannot transfer zero or negative money)

#### `DuplicateEmailException.java`
**When thrown:** In `UserServiceImpl.register()` when `userRepository.existsByEmail()` returns true — meaning someone already registered with that email.

#### `InvalidCredentialsException.java`
**When thrown:** In `UserServiceImpl.login()` when:
- No user is found with the given email, OR
- The password doesn't match the stored BCrypt hash

---

## Repository Layer — Database Access

Repositories are how the application talks to the database. In Spring Data JPA, you don't write SQL. Instead, you write Java interfaces and Spring generates all the SQL queries automatically at startup.

**How it works:**
Every repository extends `JpaRepository<EntityClass, PrimaryKeyType>`. This interface automatically provides methods like:
- `save(entity)` → INSERT or UPDATE
- `findById(id)` → SELECT WHERE id = ?
- `findAll()` → SELECT *
- `delete(entity)` → DELETE
- `count()` → SELECT COUNT(*)

---

### `repo/AccountRepository.java`

**What is it?**
```java
public interface AccountRepository extends JpaRepository<Account, Long> {
    // No extra methods needed — standard CRUD is sufficient
}
```

**Why does it have no custom methods?**
The application only needs to:
- Find an account by ID → `findById(id)` (already provided by JpaRepository)
- Save an account after balance update → `save(account)` (already provided)

That's it. Spring generates everything automatically.

**When is it used?**
- `AccountServiceImpl` → fetch account for display
- `TransferServiceImpl` → fetch both accounts, then save updated balances
- `UserServiceImpl` → save newly created account during registration, and fetch it during login

---

### `repo/UserRepository.java`

**What is it?**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**The two custom methods:**
| Method | Generated SQL | When used |
|---|---|---|
| `findByEmail(email)` | `SELECT * FROM users WHERE email = ?` | During login — to find the user and check their password |
| `existsByEmail(email)` | `SELECT COUNT(*) > 0 FROM users WHERE email = ?` | During registration — to check if the email is already taken |

**How does Spring know what SQL to generate?**
Spring Data JPA parses the method name. `findBy` + `Email` → it looks for a field named `email` in the `User` entity and generates the appropriate WHERE clause. This is called **method name derivation** — it's one of the most powerful features of Spring Data JPA.

---

### `repo/TransactionLogRepository.java`

**What is it?**
```java
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
    Optional<TransactionLog> findByIdempotencyKey(UUID idempotencyKey);
    List<TransactionLog> findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(long fromId, long toId);
}
```

**Custom methods explained:**

`findByIdempotencyKey(UUID key)`
- **Generated SQL:** `SELECT * FROM transaction_logs WHERE idempotency_key = ?`
- **Used in:** `TransferServiceImpl.checkIdempotency()` — before processing a transfer, the system checks if this UUID was already used. If found, it throws `DuplicateTransferException`.

`findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(long fromId, long toId)`
- **Generated SQL:** `SELECT * FROM transaction_logs WHERE from_account = ? OR to_account = ? ORDER BY created_on DESC`
- **Used in:** `AccountServiceImpl.getTransactions()` — the History page shows all transactions where the user was either the sender OR the receiver, sorted newest-first.
- **Why pass the same `id` twice?** The method has two parameters (`fromId` and `toId`) for the `OR` condition. You pass the same account ID for both to get all transactions involving that account.

---

### `repo/RewardLedgerRepository.java`

**What is it?**
```java
public interface RewardLedgerRepository extends JpaRepository<RewardLedger, Long> {
    List<RewardLedger> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COALESCE(SUM(r.pointsAwarded), 0) FROM RewardLedger r WHERE r.accountId = :accountId")
    int sumPointsByAccountId(@Param("accountId") Long accountId);
}
```

**Custom methods explained:**

`findByAccountIdOrderByCreatedAtDesc(accountId)`
- **Generated SQL:** `SELECT * FROM reward_ledger WHERE account_id = ? ORDER BY created_at DESC`
- **Used in:** `RewardService.getRewardHistory()` and then by `AccountController` for the rewards page

`sumPointsByAccountId(@Param("accountId") accountId)` with `@Query`
- **Why not method name derivation?** SUM is an aggregation function. Spring Data JPA can't derive this from a method name — you have to write JPQL (Java Persistence Query Language) manually.
- **JPQL explained:** `SELECT COALESCE(SUM(r.pointsAwarded), 0)` — sum all `pointsAwarded` values for a given account. `COALESCE(..., 0)` means "if the result is NULL (no rows found), return 0 instead."
- **Used in:** `RewardService.getRewardBalance()` — to display the total reward points balance

---

## Service Layer — Business Logic

The service layer is the heart of the application. Controllers receive HTTP requests and pass them to services. Services contain the actual business rules and orchestrate all the work.

---

### `service/UserService.java` — User Service Interface

**What is it?**
```java
public interface UserService {
    String register(UserRegistrationRequest request);
    UserLoginResponse login(UserLoginRequest request);
}
```

**Why use an interface instead of just a class?**
This is the **Interface Segregation** pattern from SOLID principles. By defining an interface, you decouple the controller from the implementation. The controller only knows about `UserService` (the contract), not `UserServiceImpl` (the actual code). This makes:
- **Testing easier** — you can inject a mock `UserService` in tests
- **Swapping implementations easier** — you could have `UserServiceImpl`, `UserServiceImplV2`, etc.

---

### `service/UserServiceImpl.java` — User Service Implementation

**What is it?**
The concrete implementation of `UserService`. Handles user registration and login.

**Registration flow (`register` method):**
```
1. Check if email already exists → if yes, throw DuplicateEmailException
2. Create a new Account object (holderName = name, balance = 0, status = ACTIVE)
3. Save the Account to database → get back the auto-generated account ID
4. Hash the password using BCrypt (passwordEncoder.encode)
5. Create a new User object with the hashed password and the account ID
6. Save the User to database
7. Return success message with the new account ID
```

**Login flow (`login` method):**
```
1. Find user by email → if not found, throw InvalidCredentialsException
2. Check if input password matches stored BCrypt hash → if not, throw InvalidCredentialsException
3. Load the linked Account (to get holderName and account ID)
4. Return UserLoginResponse (accountId, holderName, "Login successful")
```

**`@Transactional` on register:**
This annotation means that if anything fails midway (e.g., the User save fails after the Account save), the entire operation is rolled back. The database is left in a clean state — you won't have orphaned accounts with no users.

**BCrypt password comparison:**
```java
passwordEncoder.matches(plainPassword, storedHash)
```
`matches()` takes the plain text password the user typed, runs it through BCrypt with the same salt that was used during registration, and compares the result to the stored hash. If they match, the password is correct.

---

### `service/AccountService.java` — Account Service Interface

```java
public interface AccountService {
    AccountResponse getAccount(long id);
    BigDecimal getBalance(long id);
    List<TransactionLog> getTransactions(long id);
}
```

Three read-only operations for:
1. Getting full account details (for dashboard/profile)
2. Getting just the balance (for quick balance display)
3. Getting all transaction history

---

### `service/AccountServiceImpl.java` — Account Service Implementation

**What is it?**
Straightforward read operations. Fetches data from repositories and returns it.

**`getAccount(id)`:**
- Calls `accountRepository.findById(id)`
- If account doesn't exist, throws `AccountNotFoundException`
- Converts `Account` entity to `AccountResponse` DTO (using `AccountResponse.fromAccount()`) and returns it

**Why convert to a DTO instead of returning the entity directly?**
The `Account` entity has a `@Version` field for optimistic locking. You don't want to expose that internal detail to the frontend. The `AccountResponse` DTO only contains the fields the frontend needs. This is called **information hiding**.

**`getTransactions(id)`:**
- Calls `transactionLogRepository.findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id)`
- Returns the list directly (the entity is already a good shape for the frontend in this case)

---

### `service/TransferService.java` — Transfer Service Interface

```java
public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}
```

A single method that initiates a money transfer.

---

### `service/TransferServiceImpl.java` — Transfer Service Implementation ⭐

**This is the most complex and important file in the backend.** It orchestrates the entire money transfer process.

**The `transfer()` method (main entry point):**
```java
@Transactional
public TransferResponse transfer(TransferRequest request) {
    validateTransfer(request);        // Step 1: Basic validations
    checkIdempotency(request.idempotencyKey()); // Step 2: Duplicate check
    TransactionLog log = executeTransfer(request); // Step 3: Do the actual transfer
    return buildSuccessResponse(log); // Step 4: Build response
}
```

**Step 1 — `validateTransfer()`:**
```java
if (request.fromAccountId() == request.toAccountId()) throw new InvalidTransferException();
if (request.amount() <= 0) throw new InvalidTransferException();
```
Quick validation before touching the database. Fail fast.

**Step 2 — `checkIdempotency()`:**
```java
transactionLogRepository.findByIdempotencyKey(idempotencyKey)
    .ifPresent(log -> { throw new DuplicateTransferException(idempotencyKey); });
```
Before anything else, check if this UUID was already processed. If yes, reject immediately.

**Step 3 — `executeTransfer()` — the core logic:**
```java
// Load both accounts from database
Account fromAccount = accountRepository.findById(fromId).orElseThrow(...);
Account toAccount = accountRepository.findById(toId).orElseThrow(...);

// Check both are ACTIVE (throws if not)
if (!fromAccount.isActive()) throw new AccountNotActiveException(fromId);
if (!toAccount.isActive()) throw new AccountNotActiveException(toId);

// Perform the actual money movement (throws if insufficient balance)
fromAccount.debit(amount);   // Subtracts from sender
toAccount.credit(amount);    // Adds to receiver

// Build the transaction log
TransactionLog log = new TransactionLog(fromId, toId, ..., TransactionStatus.SUCCESS, idempotencyKey);

// Save updated balances to database
accountRepository.save(fromAccount);
accountRepository.save(toAccount);

// Award reward points if eligible
rewardService.evaluateAndGrant(log);

// Save the transaction log (also saves the idempotency key)
return transactionLogRepository.save(log);
```

**Error handling in `executeTransfer()` — the most clever part:**
```java
} catch (Exception e) {
    // Create a FAILED transaction log
    TransactionLog failedLog = new TransactionLog(..., TransactionStatus.FAILED, idempotencyKey);
    failedLog.setFailureReason(e.getMessage());
    
    // Use a NEW transaction to save the failed log
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
    template.executeWithoutResult(status -> {
        transactionLogRepository.save(failedLog);
    });
    
    throw e; // Re-throw so GlobalExceptionHandler sends proper error response
}
```

**Why `PROPAGATION_REQUIRES_NEW`?**
The outer `@Transactional` method is rolling back because an exception was thrown (e.g., InsufficientBalanceException). If you try to save the failed log inside the same transaction, that save will also be rolled back — you'd lose the failure record.

`PROPAGATION_REQUIRES_NEW` **suspends the current (rolling back) transaction** and opens a brand new, independent transaction just to save the failure log. This new transaction commits successfully, then the original transaction rolls back. Result: the failure is permanently recorded in the database even though the transfer itself didn't happen.

**Why is this important?**
The History page shows failed transactions with their reason. Users need to see "Transfer to Account #99 failed — account not found." Without `PROPAGATION_REQUIRES_NEW`, this record would be lost.

---

### `service/RewardServiceInterface.java` and `service/RewardService.java`

**What is it?**
The reward system awards points to users who make transfers above a threshold.

**Reward rules:**
1. The transaction status must be `SUCCESS`
2. The amount must be ≥ ₹100 (the `REWARD_THRESHOLD`)
3. The sender and receiver must be different accounts (no self-transfers)

**Points formula:**
```
points = floor(amount / 100)
```
For example: ₹350 → 3 points, ₹1000 → 10 points, ₹99 → 0 points (ineligible)

**`evaluateAndGrant(TransactionLog)` method:**
This is called by `TransferServiceImpl` after a successful transfer. It:
1. Checks eligibility using `isEligible()`
2. Computes points using `transaction.getAmount().divideToIntegralValue(RUPEES_PER_POINT).intValue()`
3. Creates a `RewardLedger` entry with the description
4. Saves it to the database

**`getRewardHistory(accountId)`:**
Used by `AccountController` → returns all reward entries for an account, newest first.

**`getRewardBalance(accountId)`:**
Returns the total accumulated points by summing all `pointsAwarded` for an account.

**`@Component("RewardService")`:**
The class is annotated with `@Component` (instead of `@Service`), but with an explicit name. This allows Spring to inject it using `@Autowired RewardService rewardService` without ambiguity.

---

## Controller Layer — HTTP Layer

Controllers are the **entry points** for HTTP requests. They receive a request, call the appropriate service method, and return a response. Controllers should NOT contain business logic — they are only responsible for handling HTTP-specific concerns.

---

### `controller/UserController.java`

**Route prefix:** `/api/v1/users` — these are **public** endpoints (no authentication required).

**Why are these public?**
You can't log in if the login endpoint requires you to already be logged in! The `SpringSecurityConfig` explicitly marks `/api/v1/users/**` as `permitAll()`.

**Endpoints:**

`POST /api/v1/users/register`
- Accepts `UserRegistrationRequest` in the request body
- `@Valid` annotation triggers Bean Validation — Spring automatically checks `@NotBlank`, `@Email`, `@Size` annotations on the DTO
- Calls `userService.register(request)`
- Returns HTTP 201 Created with `{"message": "Registration successful. Account #3 has been created."}`

`POST /api/v1/users/login`
- Accepts `UserLoginRequest` in the request body
- Calls `userService.login(request)`
- Returns HTTP 200 OK with `UserLoginResponse` (accountId, holderName, message)

---

### `controller/AccountController.java`

**Route prefix:** `/api/v1/accounts` — these are **protected** endpoints (require authentication).

**Endpoints:**

`GET /api/v1/accounts/{id}`
- Returns full `AccountResponse` for the given account ID

`GET /api/v1/accounts/{id}/balance`
- Returns just the balance as a `BigDecimal`

`GET /api/v1/accounts/{id}/transactions`
- Returns a `List<TransactionLog>` for the account (all transactions where they were sender or receiver)

`GET /api/v1/accounts/rewards/{accountId}`
- Returns a `List<RewardLedger>` — the reward history for the account

**`@CrossOrigin(origins = "*")`:**
This is an additional CORS annotation at the controller level. CORS (Cross-Origin Resource Sharing) is a browser security feature that blocks requests from different origins. The Angular app runs at `http://localhost:4200` and the backend at `http://localhost:8080` — these are different origins. This annotation allows the browser to make cross-origin requests.

**Note:** The global CORS config in `SpringSecurityConfig` already handles this. This `@CrossOrigin` annotation is somewhat redundant but harmless.

---

### `controller/TransferController.java`

**Route prefix:** `/api/v1/transfers` — **protected** endpoint.

**Endpoint:**

`POST /api/v1/transfers`
- Accepts `TransferRequest` in the body
- `@Valid` validates that amount is ≥ 0.01 and all fields are non-null
- Calls `transferService.transfer(request)`
- Returns HTTP 200 OK with `TransferResponse`

**Note:** The HTTP status for a successful transfer is 200 (OK) not 201 (Created). The transfer doesn't create a new "resource" per se — it modifies existing resources (account balances). 200 is appropriate here.

---

### `controller/RewardController.java`

**What is it?**
This class exists but is currently empty. The reward-related endpoint (`GET /api/v1/accounts/rewards/{accountId}`) was implemented in `AccountController` instead.

```java
public class RewardController {
    // Empty — rewards are served from AccountController
}
```

This is not a problem — it's a design choice. The file can be used in future iterations.

---

## Application Layer — DTOs

DTOs (Data Transfer Objects) are simple classes that carry data between layers. The key rule: **DTOs should not contain any business logic**. They only hold data.

---

### `application/dto/UserRegistrationRequest.java`

**What is it?**
The JSON body that the frontend sends when a user signs up.

```json
{
  "name": "Ann Maria Thomas",
  "email": "ann@example.com",
  "password": "securePass123"
}
```

**Fields with validation:**
- `@NotBlank` on `name` — cannot be empty or just spaces
- `@NotBlank` + `@Email` on `email` — must be a valid email format
- `@NotBlank` + `@Size(min = 6)` on `password` — must be at least 6 characters

When `@Valid` is used in the controller method, Spring automatically validates these constraints before the method body runs. If validation fails, Spring returns `400 Bad Request` with details about what failed.

---

### `application/dto/UserLoginRequest.java`

JSON body for login.
```json
{
  "email": "ann@example.com",
  "password": "securePass123"
}
```
Same validation annotations as above.

---

### `application/dto/UserLoginResponse.java`

What the backend sends back after a successful login.
```json
{
  "accountId": 3,
  "holderName": "Ann Maria Thomas",
  "message": "Login successful."
}
```
The frontend saves `accountId` and `holderName` in localStorage to use throughout the session.

---

### `application/dto/TransferRequest.java`

**This uses Java `record` syntax** (a modern, concise way to define immutable data classes):

```java
public record TransferRequest(
    @NotNull Long fromAccountId,
    @NotNull Long toAccountId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull UUID idempotencyKey
) {}
```

A Java record automatically generates:
- Constructor
- Getters (accessed as `request.fromAccountId()` not `request.getFromAccountId()`)
- `equals()`, `hashCode()`, `toString()`

**`@DecimalMin("0.01")`** — validation annotation ensuring the amount is at least ₹0.01 (you can't transfer ₹0 or negative amounts).

---

### `application/dto/TransferResponse.java`

What the backend sends back after a transfer:
```java
public record TransferResponse(
    String transactionId,       // "TRX-<UUID>"
    TransactionStatus status,   // SUCCESS or FAILED
    String message,             // "Transfer completed"
    long debitedFrom,           // fromAccountId
    long creditedTo,            // toAccountId
    String fromAccountName,
    String toAccountName,
    BigDecimal amount
) {}
```

---

### `application/dto/AccountResponse.java`

Safe view of the `Account` entity to send to the frontend. Uses a factory method pattern:

```java
public static AccountResponse fromAccount(Account account) {
    return new AccountResponse(
        account.getId(),
        account.getHolderName(),
        account.getBalance(),
        account.getStatus(),
        account.getLastUpdated()
    );
}
```

**Why not just return the `Account` entity directly?**
The `Account` entity has a `@Version` field (for optimistic locking) that is internal and should not be exposed to clients. By converting to `AccountResponse`, we control exactly what fields the frontend sees.

---

### `application/dto/ErrorResponse.java`

A simple record for error responses:
```java
public record ErrorResponse(String error, String message) {}
```

Used by `GlobalExceptionHandler` to return consistent error JSON:
```json
{
  "error": "TRX-400",
  "message": "Insufficient balance in account 42. Attempted: 500, Available: 100"
}
```

---

## Security Layer

### `security/SpringSecurityConfig.java` — Security Configuration

**What is it?**
This is where Spring Security is configured. It controls which endpoints require authentication and what type of authentication to use.

**`BCryptPasswordEncoder` bean:**
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
By declaring this as a `@Bean`, Spring makes it available for injection anywhere in the app. `UserServiceImpl` injects it to hash passwords during registration and compare passwords during login.

BCrypt is a **one-way hashing algorithm** specifically designed for passwords. It is intentionally slow (to prevent brute force attacks) and includes a random **salt** (extra data mixed in) to prevent rainbow table attacks.

**`UserDetailsService` bean:**
```java
@Bean
public UserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
    UserDetails admin = User.withUsername("admin")
            .password(encoder.encode("1234"))
            .roles("ADMIN")
            .build();
    return new InMemoryUserDetailsManager(admin);
}
```
Spring Security needs to know who "admin" is when it receives `Authorization: Basic YWRtaW46MTIzNA==`. This sets up an in-memory user store with a single admin user. The password must be BCrypt-encoded because we registered `BCryptPasswordEncoder` as a bean (Spring Security uses it to verify all passwords).

**Security filter chain:**
```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());  // Disable CSRF for REST APIs (stateless)
    http.cors(Customizer.withDefaults()); // Apply CORS config from corsConfigurationSource()

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow browser preflight requests
        .requestMatchers("/api/v1/users/**").permitAll()          // Register/Login = public
        .anyRequest().authenticated()                             // Everything else = needs auth
    );

    http.httpBasic(Customizer.withDefaults()); // Use HTTP Basic authentication
    return http.build();
}
```

**Why disable CSRF?**
CSRF (Cross-Site Request Forgery) protection is designed for browser-based applications that use session cookies. This application uses stateless HTTP Basic Auth (no cookies), so CSRF is not applicable. Disabling it removes the requirement to send a CSRF token with every request.

**HTTP Basic Auth explained:**
The client encodes `username:password` as Base64 and sends it in the `Authorization` header:
```
Authorization: Basic YWRtaW46MTIzNA==
```
(`YWRtaW46MTIzNA==` decodes to `admin:1234`)

Spring Security intercepts every request, extracts this header, decodes it, finds the user in the `UserDetailsService`, and verifies the password using `BCryptPasswordEncoder`.

**CORS configuration:**
```java
configuration.setAllowedOrigins(List.of("http://localhost:4200"));
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("*"));
configuration.setAllowCredentials(true);
```
Allows the Angular app (running at `localhost:4200`) to make requests to the Spring Boot backend (at `localhost:8080`). In production, you would replace `localhost:4200` with your actual frontend domain.

---

## Maven Wrapper Scripts — `mvnw` and `mvnw.cmd`

**What are they?**
These are shell scripts (Unix: `mvnw`, Windows: `mvnw.cmd`) that let you run Maven commands **without having Maven installed** on your machine. The script automatically downloads the correct version of Maven the first time you run it.

**When to use:**
```bash
./mvnw spring-boot:run   # Start the application
./mvnw package           # Build a .jar file for deployment
./mvnw test              # Run all tests
```

---

## Summary — How Everything Connects

```
HTTP Request (from Angular)
        ↓
  [Spring Security Filter]     ← Checks Authorization header
        ↓ (if allowed)
  [Controller]                 ← Maps URL to Java method
        ↓
  [Service]                    ← Runs business rules
    ↓        ↓
[Repository]  [RewardService]  ← Talks to database
    ↓
[MySQL Database]               ← Reads/writes data
    ↑
[Entity (User/Account/etc)]    ← Maps Java ↔ Database
        ↑
[DTO (Request/Response)]       ← What frontend sends/receives
        ↑
[GlobalExceptionHandler]       ← Converts exceptions to HTTP responses
```

Every piece has a single, clear responsibility. When a bug occurs, you know exactly which layer to look in.
