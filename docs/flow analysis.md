# Full Flow Analysis — Money Transfer System (Spring Boot + Angular)

> **Audience:** This document is written for beginner full-stack developers. It traces every user action from the moment a button is clicked in the browser, through the network, into the backend server, into the database, and all the way back. After reading this, you should be able to answer "exactly what happens when I click X?"

---

## 1. Project Overview

**What is this system?**
A full-stack web application that allows users to register, log in, view their account balance, transfer money between accounts, view transaction history, and earn reward points for eligible transfers.

**Two main parts:**
- **Backend:** `backend/MoneyTransferSystem` — Spring Boot app running at `http://localhost:8080`
- **Frontend:** `frontend/mts-ui` — Angular app running at `http://localhost:4200`

**How they talk to each other:**
The Angular app makes HTTP requests (GET, POST) to the Spring Boot REST API. The browser enforces CORS (Cross-Origin Resource Sharing) — since the two apps run on different ports, Spring Boot is explicitly configured to allow requests from `http://localhost:4200`.

---

## 2. System Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                    USER'S BROWSER                             │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Angular Application                      │    │
│  │  (http://localhost:4200)                              │    │
│  │                                                       │    │
│  │  Components ──► Services ──► AuthInterceptor         │    │
│  │  (UI / HTML)    (HTTP calls)   (adds Auth header)    │    │
│  └──────────────────────────────────┬────────────────────┘   │
│                                     │ HTTP Requests           │
└─────────────────────────────────────┼──────────────────────── ┘
                                      │
                                      │ (internet / localhost)
                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot Backend                          │
│                  (http://localhost:8080)                      │
│                                                              │
│  Spring Security Filter → Controller → Service → Repository │
│                                            ↕                 │
│                                         MySQL               │
│                                   (jdbc:mysql://...mts_test) │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Database Tables

Before tracing flows, understand what data lives in the database:

| Table | Java Entity | What it stores |
|---|---|---|
| `users` | `User.java` | Login credentials (email, bcrypt-hashed password, linked account ID) |
| `accounts` | `Account.java` | Account balance, holder name, status (ACTIVE/LOCKED/CLOSED) |
| `transaction_logs` | `TransactionLog.java` | Every transfer attempt (success + failure) with idempotency keys |
| `reward_ledger` | `RewardLedger.java` | Points awarded for eligible transfers |

**Key relationships:**
- One `User` → exactly one `Account` (via `user.account_id` foreign key)
- One `TransactionLog` → references two `Account` rows (from and to)
- One `RewardLedger` → references one `Account` (who earned points) and one `TransactionLog` (which transfer triggered it)

---

## 4. Security Model

**How authentication works in this app:**

The backend uses **HTTP Basic Authentication**. Every API call (except register/login) must include an `Authorization` header:
```
Authorization: Basic YWRtaW46MTIzNA==
```
`YWRtaW46MTIzNA==` is the Base64 encoding of `admin:1234`.

**Why does the frontend use `admin:1234` credentials for all users?**
This is a simplified design for learning. The application has two levels of "who you are":
1. **HTTP-level authentication** → verified by Spring Security → uses `admin:1234` for all logged-in users
2. **Application-level identity** → verified by the `/api/v1/users/login` endpoint → the accountId stored in localStorage identifies which account to show data for

In a real production app, you would use per-user JWT tokens instead of shared credentials.

**Which endpoints are public vs. protected?**
| Endpoint | Access |
|---|---|
| `POST /api/v1/users/register` | Public — no auth needed |
| `POST /api/v1/users/login` | Public — no auth needed |
| `GET /api/v1/accounts/**` | Protected — requires `admin:1234` Basic Auth |
| `POST /api/v1/transfers` | Protected — requires `admin:1234` Basic Auth |

**The `AuthInterceptor` automatically adds the `Authorization` header** to all HTTP requests except the user endpoints. Components and services don't need to think about this — it happens transparently.

---

## 5. Flow 1: User Registration

**User action:** User fills in Name, Email, Password, Confirm Password on the Sign Up tab and clicks "Register."

### Step-by-step trace:

**Step 1 — Form Validation (Browser)**

`StartPageComponent.onSignup()` is called. Angular's `ReactiveFormsModule` checks:
- `name` is not empty (`Validators.required`)
- `email` is a valid email format (`Validators.email`)
- `password` is at least 6 characters (`Validators.minLength(6)`)
- `password === confirmPassword` (`passwordMatchValidator` cross-field check)

If any check fails, the form is marked `invalid` and the function returns early. No API call is made.

**Step 2 — HTTP Call to Backend**

`UserService.register({ name, email, password })` is called.

```typescript
return this.http.post<{ message: string }>('http://localhost:8080/api/v1/users/register', request);
```

The `AuthInterceptor` intercepts this request. It checks:
```typescript
const isPublicUserEndpoint = req.url.includes('/api/v1/users/');
```
→ `true`, so **no Authorization header is added**. The request goes out without authentication.

**Step 3 — Spring Security Filter (Backend)**

Spring Security receives the incoming request. It checks the security filter chain in `SpringSecurityConfig`:
```java
.requestMatchers("/api/v1/users/**").permitAll()
```
→ This URL matches, so the request is allowed through **without requiring any credentials**.

**Step 4 — `UserController.register()` is called**

```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
    String message = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
}
```

`@Valid` triggers Bean Validation on `UserRegistrationRequest`:
- `@NotBlank` on `name`, `email`, `password`
- `@Email` on `email`
- `@Size(min = 6)` on `password`

If any check fails, Spring automatically returns `400 Bad Request` with details — `userService.register()` is never called.

**Step 5 — `UserServiceImpl.register()` executes the business logic**

```java
@Transactional
public String register(UserRegistrationRequest request) {
```

`@Transactional` wraps everything in a database transaction. If anything fails below, all database changes are rolled back.

Sub-step 5a: **Check for duplicate email**
```java
if (userRepository.existsByEmail(request.getEmail())) {
    throw new DuplicateEmailException(request.getEmail());
}
```
→ SQL: `SELECT COUNT(*) > 0 FROM users WHERE email = ?`
→ If returns true → throw `DuplicateEmailException` → `GlobalExceptionHandler` returns `409 Conflict`

Sub-step 5b: **Create the Account first**
```java
Account account = new Account();
account.setHolderName(request.getName());
account.setBalance(BigDecimal.ZERO);
account.setStatus(AccountStatus.ACTIVE);
account.setLastUpdated(LocalDateTime.now());
Account savedAccount = accountRepository.save(account);
```
→ SQL: `INSERT INTO accounts (holder_name, balance, status, version, last_updated) VALUES (?, 0, 'ACTIVE', 0, ?)`
→ MySQL generates the auto-increment `id` (e.g., `5`) and returns it in `savedAccount.getId()`

Sub-step 5c: **Hash the password and create the User**
```java
String hashedPassword = passwordEncoder.encode(request.getPassword());
User user = new User(request.getName(), request.getEmail(), hashedPassword, savedAccount.getId());
userRepository.save(user);
```
→ `passwordEncoder.encode("myPassword123")` → generates something like `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
→ SQL: `INSERT INTO users (name, email, password_hash, account_id, created_at) VALUES (?, ?, ?, ?, ?)`

Sub-step 5d: **Return success message**
```java
return "Registration successful. Account #" + savedAccount.getId() + " has been created.";
```

**Step 6 — Response travels back to the browser**

Controller returns `201 Created`:
```json
{ "message": "Registration successful. Account #5 has been created." }
```

**Step 7 — `StartPageComponent` handles the response**

```typescript
next: (res) => {
  this.signupSuccess = res.message + ' You can now sign in.';
  this.signupForm.reset();
  // After 2 seconds, switch to login tab
  setTimeout(() => { this.setTab('login'); }, 2000);
}
```
→ User sees a success message and is redirected to the login tab.

---

## 6. Flow 2: User Login

**User action:** User enters email and password on the Sign In tab and clicks "Sign In."

### Step-by-step trace:

**Step 1 — Form Validation**

`StartPageComponent.onLogin()` checks:
- `email` is valid
- `password` is not empty

**Step 2 — HTTP Call**

`UserService.login({ email, password })` → `POST /api/v1/users/login`
→ `AuthInterceptor`: public endpoint → no auth header added

**Step 3 — Spring Security**: `permitAll()` → request passes through

**Step 4 — `UserController.login()` is called**

```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
    UserLoginResponse response = userService.login(request);
    return ResponseEntity.ok(response);
}
```

**Step 5 — `UserServiceImpl.login()` validates credentials**

Sub-step 5a: **Find user by email**
```java
User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new InvalidCredentialsException());
```
→ SQL: `SELECT * FROM users WHERE email = ?`
→ If no user found → throw `InvalidCredentialsException` → `GlobalExceptionHandler` returns `401 Unauthorized`

Sub-step 5b: **Verify password**
```java
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new InvalidCredentialsException();
}
```
`passwordEncoder.matches("myPassword123", "$2a$10$N9qo8...")`:
- BCrypt extracts the salt from the stored hash
- Re-hashes the input password with that salt
- Compares the result to the stored hash
- Returns `true` if they match, `false` if not

If false → `InvalidCredentialsException` → `401 Unauthorized`

Sub-step 5c: **Load the linked Account**
```java
Account account = accountRepository.findById(user.getAccountId())
        .orElseThrow(() -> new RuntimeException("Linked account not found."));
```
→ SQL: `SELECT * FROM accounts WHERE id = ?`

Sub-step 5d: **Build and return the response**
```java
return new UserLoginResponse(account.getId(), account.getHolderName(), "Login successful.");
```

**Step 6 — Response back to browser**
```json
{ "accountId": 5, "holderName": "Ann Maria Thomas", "message": "Login successful." }
```

**Step 7 — `StartPageComponent` establishes the session**

```typescript
next: (res: UserLoginResponse) => {
  this.authService.setUserSession(res.holderName, res.accountId);
  this.router.navigate(['/dashboard']);
}
```

`authService.setUserSession('Ann Maria Thomas', 5)` does:
```typescript
const token = btoa('admin:1234');  // → 'YWRtaW46MTIzNA=='
localStorage.setItem('auth_token', 'YWRtaW46MTIzNA==');
localStorage.setItem('auth_user', 'Ann Maria Thomas');
localStorage.setItem('account_id', '5');
this.loggedIn.next(true);
```

**Result:** The user is now "logged in." The browser has the token and account ID stored. Angular routes to `/dashboard`.

---

## 7. Flow 3: Loading the Dashboard

**User action:** After login, the browser navigates to `/dashboard`.

### Step-by-step trace:

**Step 1 — Angular Router checks the route**

Route definition:
```typescript
{ path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] }
```

`AuthGuard.canActivate()` is called:
```typescript
canActivate(): boolean {
  if (this.authService.isLoggedIn()) return true;  // localStorage has a token → true
  this.router.navigate(['/start']);
  return false;
}
```
→ Token exists → `true` → navigation is allowed → `DashboardComponent` is rendered

**Step 2 — `DashboardComponent.ngOnInit()` fires**

```typescript
ngOnInit(): void {
  this.loadAccount();
}

loadAccount() {
  const accId = this.authService.getAccountId() ?? 0;  // → 5 (from localStorage)
  this.accountService.getAccount(accId).subscribe((data) => {
    if (data) this.account.set(data);
  });
}
```

**Step 3 — `AccountService.getAccount(5)` sends HTTP request**

```typescript
return this.http.get<Account>('http://localhost:8080/api/v1/accounts/5')
```

**Step 4 — `AuthInterceptor` adds the auth header**

```typescript
const isPublicUserEndpoint = req.url.includes('/api/v1/users/'); // → false
// Token exists and it's not a public endpoint:
authReq = req.clone({
  setHeaders: { Authorization: 'Basic YWRtaW46MTIzNA==' }
});
```
The request now has the header attached.

**Step 5 — Spring Security validates the auth header**

Spring Security parses `Authorization: Basic YWRtaW46MTIzNA==`:
1. Base64 decodes it → `admin:1234`
2. Looks up `admin` in `UserDetailsService` (the in-memory store configured in `SpringSecurityConfig`)
3. Uses `BCryptPasswordEncoder` to verify `1234` against the stored BCrypt hash
4. Match → authentication successful → request allowed to proceed to controller

**Step 6 — `AccountController.getAccountById(5)` is called**

```java
@GetMapping("/{id}")
public ResponseEntity<AccountResponse> getAccountById(@PathVariable long id) {
    return new ResponseEntity<>(service.getAccount(id), HttpStatus.OK);
}
```

**Step 7 — `AccountServiceImpl.getAccount(5)` fetches data**

```java
public AccountResponse getAccount(long id) {
    Account account = accountRepository.findById(id)
        .orElseThrow(() -> new AccountNotFoundException(id));
    return AccountResponse.fromAccount(account);
}
```
→ SQL: `SELECT id, holder_name, balance, status, version, last_updated FROM accounts WHERE id = 5`
→ Creates `AccountResponse(5, "Ann Maria Thomas", 1500.00, ACTIVE, "2024-01-15T10:30:00")`
→ **Note:** `version` is NOT included in `AccountResponse` — it's internal data

**Step 8 — Response back to browser**

```json
{
  "id": 5,
  "holderName": "Ann Maria Thomas",
  "balance": 1500.00,
  "status": "ACTIVE",
  "lastUpdated": "2024-01-15T10:30:00"
}
```

**Step 9 — `DashboardComponent` updates the UI**

```typescript
this.account.set(data);  // Signal update → triggers re-render
```
Angular's change detection sees the signal changed and re-renders the template with the new data. The user sees their balance and account name on screen.

---

## 8. Flow 4: Money Transfer (Core Flow ⭐)

**User action:** User navigates to `/transfer`, enters recipient account ID `7` and amount `₹500`, and clicks "Transfer Now."

### Step-by-step trace:

**Step 1 — Route Guard**: `AuthGuard` passes (token in localStorage)

**Step 2 — `TransferComponent.ngOnInit()` fires**

```typescript
// Load sender's current balance
this.accountService.getAccount(accountId).subscribe({
  next: (acc) => { this.availableBalance = acc.balance; }
});

// Set up live recipient name lookup
this.toAccountSub = this.transferForm.get('toAccountId')?.valueChanges.pipe(
  debounceTime(400),
  switchMap(val => this.accountService.getAccount(Number(val)))
).subscribe(account => {
  this.recipientName = `Recipient: ${account.holderName}`;
});
```

As the user types `7` in the recipient field, after 400ms of inactivity:
- `GET /api/v1/accounts/7` is called
- Response: `{ holderName: "Bob Smith", ... }`
- UI shows: `"Recipient: Bob Smith"`

**Step 3 — User clicks "Transfer Now", `submitTransfer()` fires**

```typescript
const request: TransferRequest = {
  fromAccountId: 5,                       // From localStorage
  toAccountId: 7,                         // From form
  amount: 500,                            // From form
  idempotencyKey: crypto.randomUUID(),    // e.g., "f47ac10b-58cc-4372-a567-0e02b2c3d479"
};
this.transferService.transfer(request).subscribe({ ... });
```

**Step 4 — `TransferService.transfer()` sends HTTP request**

```typescript
return this.http.post<TransferResponse>('http://localhost:8080/api/v1/transfers', request)
  .pipe(catchError(this.handleError));
```

**Step 5 — `AuthInterceptor` adds auth header**

Not a public user endpoint → attaches `Authorization: Basic YWRtaW46MTIzNA==`

**Step 6 — Spring Security validates** → passes (admin:1234 is valid)

**Step 7 — `TransferController.transfer()` is called**

```java
@PostMapping
public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
    TransferResponse response = transferService.transfer(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
}
```

`@Valid` checks:
- `fromAccountId` is not null
- `toAccountId` is not null
- `amount` is not null and ≥ 0.01
- `idempotencyKey` is not null

**Step 8 — `TransferServiceImpl.transfer()` — the main logic**

```java
@Transactional
public TransferResponse transfer(TransferRequest request) {
    validateTransfer(request);
    checkIdempotency(request.idempotencyKey());
    TransactionLog transactionLog = executeTransfer(request);
    return buildSuccessResponse(transactionLog);
}
```

Sub-step 8a: **`validateTransfer()`**
```java
if (request.fromAccountId() == request.toAccountId()) throw new InvalidTransferException();
// 5 ≠ 7 → passes
if (request.amount().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidTransferException();
// 500 > 0 → passes
```

Sub-step 8b: **`checkIdempotency("f47ac10b-...")`**
```java
transactionLogRepository.findByIdempotencyKey(idempotencyKey)
    .ifPresent(log -> { throw new DuplicateTransferException(idempotencyKey); });
```
→ SQL: `SELECT * FROM transaction_logs WHERE idempotency_key = 'f47ac10b-...'`
→ No result found (this is a new UUID) → passes

Sub-step 8c: **`executeTransfer()` — The actual money movement**

```java
// Load sender
Account fromAccount = accountRepository.findById(5L)
    .orElseThrow(() -> new AccountNotFoundException(5));
// SQL: SELECT * FROM accounts WHERE id = 5
// Result: { id: 5, holderName: "Ann Maria Thomas", balance: 1500.00, status: ACTIVE, version: 2 }

// Load receiver
Account toAccount = accountRepository.findById(7L)
    .orElseThrow(() -> new AccountNotFoundException(7));
// SQL: SELECT * FROM accounts WHERE id = 7
// Result: { id: 7, holderName: "Bob Smith", balance: 200.00, status: ACTIVE, version: 0 }

// Check both are ACTIVE
if (!fromAccount.isActive()) throw new AccountNotActiveException(5);
// "ACTIVE".equals("ACTIVE") → true → passes

if (!toAccount.isActive()) throw new AccountNotActiveException(7);
// passes

// DEBIT the sender
fromAccount.debit(new BigDecimal("500"));
// Inside debit():
//   isActive() → true → passes
//   balance (1500) >= amount (500) → passes
//   balance = 1500 - 500 = 1000
//   lastUpdated = now()

// CREDIT the receiver
toAccount.credit(new BigDecimal("500"));
// Inside credit():
//   isActive() → true → passes
//   balance = 200 + 500 = 700
//   lastUpdated = now()

// Create the transaction log (SUCCESS)
TransactionLog transactionLog = new TransactionLog(
    5, 7, "Ann Maria Thomas", "Bob Smith",
    new BigDecimal("500"), TransactionStatus.SUCCESS,
    UUID.fromString("f47ac10b-...")
);

// Save updated account balances
accountRepository.save(fromAccount);
// SQL: UPDATE accounts SET balance = 1000, last_updated = ?, version = 3
//      WHERE id = 5 AND version = 2  ← Optimistic lock check!

accountRepository.save(toAccount);
// SQL: UPDATE accounts SET balance = 700, last_updated = ?, version = 1
//      WHERE id = 7 AND version = 0

// Award reward points (if eligible)
rewardService.evaluateAndGrant(transactionLog);
// → 500 >= 100 → eligible → points = floor(500/100) = 5
// → INSERT INTO reward_ledger (account_id, transaction_id, points_awarded, description, created_at)
//   VALUES (5, 'f47ac10b-...', 5, 'Earned 5 point(s) for transferring ₹500 to account #7', now())

// Save the transaction log (locks in the idempotency key)
return transactionLogRepository.save(transactionLog);
// SQL: INSERT INTO transaction_logs (from_account, to_account, from_account_name, to_account_name,
//      amount, status, failure_reason, idempotency_key, created_on)
//      VALUES (5, 7, 'Ann Maria Thomas', 'Bob Smith', 500, 'SUCCESS', null, 'f47ac10b-...', now())
```

**The `@Transactional` boundary:** All of the above — the two account updates, the reward insert, and the transaction log insert — happen within **one database transaction**. If the transaction log save fails (e.g., database goes down), the account balance updates are also rolled back. The database stays consistent.

Sub-step 8d: **`buildSuccessResponse()`**
```java
return new TransferResponse(
    "TRX-" + log.getId(),          // "TRX-f47ac10b-..."
    TransactionStatus.SUCCESS,
    "Transfer completed",
    5, 7,
    "Ann Maria Thomas", "Bob Smith",
    new BigDecimal("500")
);
```

**Step 9 — Response back to browser**
```json
{
  "transactionId": "TRX-f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "status": "SUCCESS",
  "message": "Transfer completed",
  "debitedFrom": 5,
  "creditedTo": 7,
  "fromAccountName": "Ann Maria Thomas",
  "toAccountName": "Bob Smith",
  "amount": 500
}
```

**Step 10 — `TransferComponent` handles success**

```typescript
next: (response: TransferResponse) => {
  this.success = response.status === 'SUCCESS';   // true
  this.resultMessage = response.message;           // "Transfer completed"
  this.accountService.refreshAccount(5);           // Force balance refresh on dashboard
  this.showSuccessAlert = true;                    // Show success banner
  this.cdr.detectChanges();                        // Update UI immediately
  
  // Navigate to dashboard after 3 seconds
  this.redirectTimeout = setTimeout(() => {
    this.router.navigate(['/dashboard']);
  }, 3000);
}
```

---

## 9. Flow 5: Transfer Failure — Insufficient Balance

**User action:** User tries to transfer ₹2000 but has only ₹1000.

**The failure happens in `Account.debit()`:**
```java
public void debit(BigDecimal amount) {
    // balance (1000) < amount (2000)
    if (this.balance.compareTo(amount) < 0) {
        throw new InsufficientBalanceException(this.id, amount, balance);
    }
}
```

**The catch block in `executeTransfer()` handles it:**
```java
} catch (Exception e) {
    TransactionLog failedLog = new TransactionLog(
        5, 7, "Ann Maria Thomas", "Bob Smith",
        new BigDecimal("2000"), TransactionStatus.FAILED, idempotencyKey
    );
    failedLog.setFailureReason("Insufficient balance in account 5. Attempted: 2000, Available: 1000");
    
    // CRITICAL: Use a NEW independent transaction to save the failure log
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
    template.executeWithoutResult(status -> {
        transactionLogRepository.save(failedLog);  // This commits independently
    });
    
    throw e;  // Re-throw → GlobalExceptionHandler catches it
}
```

**Why `PROPAGATION_REQUIRES_NEW`?**
The outer `@Transactional` method is about to roll back because an exception was thrown. If you try to save the failure log inside the same transaction, that save would also roll back — you'd lose the failure record forever.

`PROPAGATION_REQUIRES_NEW` creates a completely separate transaction:
- The outer transaction (which modified `fromAccount.balance`) is **suspended**
- A new transaction opens
- The `failedLog` is saved and committed
- The new transaction ends
- The original (now-suspended) transaction resumes and **rolls back**

Result: 
- `accounts` table → **unchanged** (balance stays at 1000, as it should)
- `transaction_logs` table → **has a new FAILED row** with the reason

**`GlobalExceptionHandler` converts the exception to HTTP:**
```java
@ExceptionHandler(InsufficientBalanceException.class)
public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
    ErrorResponse res = new ErrorResponse("TRX-400", e.getMessage());
    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);  // 400
}
```

**HTTP response to browser:**
```json
{ "error": "TRX-400", "message": "Insufficient balance in account 5. Attempted: 2000, Available: 1000" }
```

**`TransferService.handleError()` converts to user-friendly message:**
```typescript
if (error.status === 400) {
  errorMessage = error.error?.message || 'Invalid request: Please check your input.'
}
// → "Insufficient balance in account 5. Attempted: 2000, Available: 1000"
```

**`TransferComponent` shows error toast:**
```typescript
error: (err: any) => {
  this.showErrorToast(err.message || 'Transfer failed. Please try again.');
}
```
The user sees an error toast that disappears after 5 seconds.

---

## 10. Flow 6: Duplicate Transfer (Idempotency)

**Scenario:** User clicks "Transfer Now" twice quickly. The second click sends the same idempotency key (if the form didn't reset between clicks — edge case) OR the user resends a previously successful transfer.

**The failure happens in `checkIdempotency()`:**
```java
transactionLogRepository.findByIdempotencyKey(idempotencyKey)
    .ifPresent(log -> {
        throw new DuplicateTransferException(idempotencyKey);
    });
```
→ SQL: `SELECT * FROM transaction_logs WHERE idempotency_key = 'f47ac10b-...'`
→ Found! → throw `DuplicateTransferException`

**Note:** In practice, this rarely occurs with the current frontend because `submitTransfer()` generates a fresh UUID each time the form is submitted. However, if a user's network times out after the server commits but before the response reaches the browser, they might retry with the same UUID (stored on the client). The idempotency check protects against this.

---

## 11. Flow 7: Viewing Transaction History

**User action:** User clicks "History" in the navbar.

**Step 1 — Route to `/history`**: `AuthGuard` passes

**Step 2 — `HistoryComponent.ngOnInit()`**

```typescript
this.accountId = this.authService.getAccountId() ?? 0;  // → 5
this.loadTransactions();
```

**Step 3 — `AccountService.getAccountTransactions(5)`**

```typescript
this.http.get<TransactionLog[]>('http://localhost:8080/api/v1/accounts/5/transactions')
```
→ `AuthInterceptor` adds auth header → Spring Security validates

**Step 4 — `AccountController.getTransactions(5)`**

```java
@GetMapping("/{id}/transactions")
public ResponseEntity<List<TransactionLog>> getTransactions(@PathVariable long id) {
    return new ResponseEntity<>(service.getTransactions(id), HttpStatus.OK);
}
```

**Step 5 — `AccountServiceImpl.getTransactions(5)`**

```java
public List<TransactionLog> getTransactions(long id) {
    return transactionLogRepository
        .findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);
}
```
→ SQL: `SELECT * FROM transaction_logs WHERE from_account = 5 OR to_account = 5 ORDER BY created_on DESC`
→ Returns all transactions where account 5 was either the sender OR the receiver, newest first

**Step 6 — Response and filtering**

The browser receives a list of `TransactionLog` objects. The `filteredTransactions` computed getter applies any active filters client-side — no extra API calls are needed for filtering.

---

## 12. Flow 8: Viewing Reward Points

**User action:** User navigates to `/rewards`.

**Step 1 — Lazy loading**: The `RewardComponent` is not part of the main bundle. Angular downloads it on demand:
```typescript
loadComponent: () => import('./components/reward-component/reward-component')
                       .then(m => m.RewardComponent)
```

**Step 2 — `RewardComponent.ngOnInit()` calls `loadRewards(5)`**

**Step 3 — `RewardService.getRewards(5)`**

```typescript
this.http.get<Reward[]>('http://localhost:8080/api/v1/accounts/rewards/5')
```

**Step 4 — `AccountController.viewRewards(5)`**

```java
@GetMapping("/rewards/{accountId}")
public ResponseEntity<?> viewRewards(@PathVariable long accountId) {
    return new ResponseEntity<>(rewardService.getRewardHistory(accountId), HttpStatus.OK);
}
```

**Step 5 — `RewardService.getRewardHistory(5)`**

```java
public List<RewardLedger> getRewardHistory(long accountId) {
    return rewardLedgerRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
}
```
→ SQL: `SELECT * FROM reward_ledger WHERE account_id = 5 ORDER BY created_at DESC`

**Step 6 — `RewardComponent` calculates total**

```typescript
this.rewards.set(rewards);
this.totalPoints.set(rewards.reduce((sum, r) => sum + r.pointsAwarded, 0));
```
Total points = sum of all `pointsAwarded` values in the returned list.

---

## 13. Flow 9: User Logout

**User action:** User clicks "Logout" in the navbar.

**`TopNavbarComponent.logout()`:**
```typescript
logout(): void {
  this.authService.logout();
}
```

**`AuthService.logout()`:**
```typescript
logout(): void {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('auth_user');
  localStorage.removeItem('account_id');
  localStorage.removeItem('user_role');
  this.loggedIn.next(false);     // Reactive state update
  this.router.navigate(['/start']);
}
```

No HTTP call is made to the backend. Since there are no server-side sessions (HTTP Basic Auth is stateless), logout is purely a client-side operation — clearing the stored credentials from localStorage.

---

## 14. Reward Points Evaluation — Deep Dive

This happens automatically inside `TransferServiceImpl.executeTransfer()` after a successful transfer:

```java
rewardService.evaluateAndGrant(transactionLog);
```

**`RewardService.evaluateAndGrant()` step by step:**

Step 1: **Check eligibility**
```java
private boolean isEligible(TransactionLog transaction) {
    // Rule 1: Must be SUCCESS
    if (transaction.getStatus() != TransactionStatus.SUCCESS) return false;
    
    // Rule 2: Amount must be >= ₹100
    if (transaction.getAmount().compareTo(new BigDecimal("100")) < 0) return false;
    
    // Rule 3: Must not be a self-transfer
    if (transaction.getFromAccountId() == transaction.getToAccountId()) return false;
    
    return true;
}
```

Step 2: **Calculate points**
```java
// floor(amount / 100)
int points = transaction.getAmount()
    .divideToIntegralValue(new BigDecimal("100"))
    .intValue();
// floor(500 / 100) = floor(5.0) = 5 points
```

Step 3: **Create and save reward entry**
```java
RewardLedger entry = new RewardLedger();
entry.setAccountId(transaction.getFromAccountId());  // Sender earns points
entry.setTransactionId(transaction.getId());
entry.setPointsAwarded(5);
entry.setCreatedAt(LocalDateTime.now());
entry.setDescription("Earned 5 point(s) for transferring ₹500 to account #7");
rewardLedgerRepository.save(entry);
```
→ SQL: `INSERT INTO reward_ledger (...) VALUES (...)`

**Important: This runs within the outer `@Transactional` boundary.** If the transaction log save later fails, the reward entry is also rolled back.

---

## 15. Error Handling Architecture

```
Exception thrown in Service Layer
        ↓
    @Transactional rolls back (if annotated)
        ↓
    Exception propagates up to Controller
        ↓
    @ControllerAdvice (GlobalExceptionHandler) catches it
        ↓
    Maps to ErrorResponse + HTTP status code
        ↓
    HTTP Response sent to browser
        ↓
    AuthInterceptor checks status:
      - 401? → authService.logout() → redirect to /start
      - Other error? → passes error to Observable
        ↓
    catchError in TransferService/RewardService
      → converts to user-friendly message
        ↓
    Component's error handler
      → shows toast / error message to user
```

---

## 16. Complete Data Flow Diagram

```
REGISTRATION:
Browser                    Angular                   Spring Boot           MySQL
   |                          |                          |                    |
   |-- [Fill form, click] --> |                          |                    |
   |                          |-- POST /users/register -->|                   |
   |                          |   (no auth header)        |                   |
   |                          |                          |-- SELECT users --> |
   |                          |                          |<-- empty --------- |
   |                          |                          |-- INSERT accounts ->|
   |                          |                          |<-- saved (id=5) ---  |
   |                          |                          |-- INSERT users ----> |
   |                          |                          |<-- saved ----------- |
   |                          |<-- 201 {"message":...} -- |                    |
   |<-- show success -------- |                          |                    |

LOGIN:
Browser                    Angular                   Spring Boot           MySQL
   |                          |                          |                    |
   |-- [Fill, click Sign In] >|                          |                    |
   |                          |-- POST /users/login ----> |                   |
   |                          |                          |-- SELECT users ---> |
   |                          |                          |<-- User row -------- |
   |                          |                          |-- bcrypt.matches() --|
   |                          |                          |-- SELECT accounts --> |
   |                          |                          |<-- Account row ------- |
   |                          |<-- 200 {accountId:5,...} -|                    |
   |                          |-- localStorage.set() ----|                    |
   |                          |-- navigate('/dashboard') -|                   |

TRANSFER:
Browser                    Angular                   Spring Boot           MySQL
   |                          |                          |                    |
   |-- [Fill, click Transfer] >|                         |                    |
   |                          |-- POST /transfers ------> |                   |
   |                          |   + Authorization header  |                   |
   |                          |                          |-- SELECT users ---> | (Spring Security)
   |                          |                          |<-- admin row ------- |
   |                          |                          |-- SELECT tx_logs --> | (idempotency check)
   |                          |                          |<-- empty ----------- |
   |                          |                          |-- SELECT accounts -> | (load from)
   |                          |                          |<-- Account{5} ------- |
   |                          |                          |-- SELECT accounts -> | (load to)
   |                          |                          |<-- Account{7} ------- |
   |                          |                          |-- debit(500) --------|
   |                          |                          |-- credit(500) -------|
   |                          |                          |-- UPDATE accounts -> | (save from)
   |                          |                          |-- UPDATE accounts -> | (save to)
   |                          |                          |-- INSERT reward_ledger| (if eligible)
   |                          |                          |-- INSERT tx_logs ---> | (save log)
   |                          |<-- 200 {SUCCESS,...} ---- |                    |
   |<-- show success banner -- |                          |                    |
   |<-- navigate /dashboard -- |                          |                    |
```

---

## 17. Key Design Patterns Explained

### Interface + Implementation (e.g., `TransferService` + `TransferServiceImpl`)
The controller injects `TransferService` (the interface). Spring injects the `TransferServiceImpl` (the implementation). This decouples the controller from the concrete implementation — you can swap implementations without changing the controller.

### `@Transactional` — Database Transaction Management
A transaction is an all-or-nothing unit of work. Either all SQL operations within it succeed and commit, or they all fail and roll back. `@Transactional` on `transfer()` ensures that account balance updates and transaction log creation either all happen or none of them do.

### `PROPAGATION_REQUIRES_NEW` — Nested Independent Transactions
When you need to do something that should persist even if the outer transaction fails (like saving a failure log), you open a new independent transaction. This is the critical pattern for reliable audit logging.

### Optimistic Locking (`@Version`)
Each `Account` row has a `version` column. Every UPDATE includes `WHERE version = old_version`. If two threads try to update the same row simultaneously, one will succeed (and increment the version), the other will fail (because its version number is now stale). This prevents "lost updates" without expensive database-level locks.

### Idempotency Keys
A UUID that the client generates before sending a request and the server uses to detect duplicate submissions. Essential in distributed systems where network failures can cause retries.

### HTTP Interceptor Pattern
One central piece of code that modifies all HTTP requests — adding headers, handling errors. Without this, every service would have to manually add the Authorization header. The interceptor makes authentication completely transparent to the rest of the application.

### Signals (Angular)
Angular's new reactive primitive. When a signal's value changes, Angular automatically re-renders only the parts of the template that read that signal — efficiently and without you having to manually trigger updates.

### CORS Preflight (Browser Security)
Any cross-origin request with a custom header like `Authorization` is automatically preceded by an OPTIONS request that the browser generates. The server must explicitly reply with allowed origins/methods/headers before the browser sends the real request. This is a browser security feature — no code in the application generates or controls it.

---

## 18. CORS Preflight — What It Is and How It Flows Through This Project

### What is CORS?

**CORS (Cross-Origin Resource Sharing)** is a browser security mechanism. A browser enforces a rule called the **Same-Origin Policy** — JavaScript running on one origin (protocol + domain + port) cannot make HTTP requests to a different origin.

Your app has two different origins:
```
Angular app  →  http://localhost:4200   (Origin A)
Spring Boot  →  http://localhost:8080   (Origin B)
```

The ports are different, so these are two separate origins. Normally the browser would block all requests from Angular to Spring Boot. CORS is the mechanism that lets the server say: *"I explicitly trust requests from `localhost:4200` — let them through."*

---

### What is a Preflight Request?

Before sending any "non-simple" cross-origin HTTP request, the browser automatically sends an **HTTP OPTIONS request** first to ask the server:

> *"I'm about to send a POST from `localhost:4200` with an `Authorization` header. Are you okay with that?"*

This OPTIONS call is called the **preflight**. The browser generates it automatically — there is no code in Angular or Spring Boot that creates it.

**What makes a request "non-simple" (and therefore triggers a preflight)?**

The browser triggers a preflight if the request has:
- A custom header (like `Authorization`) — **this is what applies in your project**
- A method other than GET/POST/HEAD
- A `Content-Type` other than the standard plain text types

---

### What Triggers the Preflight in This Project

The trigger is in [`auth-interceptor.ts`](file:///c:/Users/user/Desktop/Capstone/MTS_Batch3/frontend/mts-ui/src/app/interceptors/auth-interceptor.ts):

```typescript
authReq = req.clone({
  setHeaders: {
    Authorization: `Basic ${token}`  // ← This custom header triggers preflight
  }
});
```

The moment the browser sees `Authorization` as a header on a cross-origin request, it classifies the request as non-simple and **automatically generates an OPTIONS call before sending your real request**. The `AuthInterceptor` didn't intend to trigger it — it's an automatic browser side effect.

---

### The Full Preflight Flow — Step by Step

**Step 1 — Angular code calls the API**
```typescript
this.http.post('http://localhost:8080/api/v1/transfers', request)
```

**Step 2 — `AuthInterceptor` clones the request and adds the `Authorization` header**
```
POST http://localhost:8080/api/v1/transfers
Authorization: Basic YWRtaW46MTIzNA==
Content-Type: application/json
```

**Step 3 — Browser intercepts it before sending**

The browser sees:
- Different origin? Yes (4200 vs 8080) ✓
- Custom header `Authorization`? Yes ✓
- → Triggers preflight automatically

**Step 4 — Browser auto-generates and sends OPTIONS request**
```
OPTIONS http://localhost:8080/api/v1/transfers
Origin: http://localhost:4200
Access-Control-Request-Method: POST
Access-Control-Request-Headers: authorization, content-type
```

No `Authorization` header here — it's just a negotiation. No body either.

**Step 5 — Spring Security receives OPTIONS**

The security filter chain in [`SpringSecurityConfig.java`](file:///c:/Users/user/Desktop/Capstone/MTS_Batch3/backend/MoneyTransferSystem/src/main/java/com/fidelity/mts/security/SpringSecurityConfig.java) checks:

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // ← matches, allow without auth
    .requestMatchers("/api/v1/users/**").permitAll()
    .anyRequest().authenticated()
);
```

OPTIONS matches the first rule → allowed without any credentials check → passes to the CORS handler.

**Step 6 — Spring reads the CORS configuration**

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:4200")); // ← is origin trusted?
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // ← is POST allowed?
    configuration.setAllowedHeaders(List.of("*")); // ← is Authorization header allowed?
    configuration.setAllowCredentials(true);
    ...
}
```

All checks pass → Spring sends back a 200 with CORS approval headers.

**Step 7 — Backend OPTIONS response**
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

**Step 8 — Browser reads the approval**

Browser checks:
- Is `localhost:4200` in `Allow-Origin`? Yes ✓
- Is `POST` in `Allow-Methods`? Yes ✓
- Is `Authorization` covered by `Allow-Headers`? Yes (`*` = all) ✓
- → Preflight approved → browser now sends the real request

**Step 9 — Browser sends the actual POST**
```
POST http://localhost:8080/api/v1/transfers
Origin: http://localhost:4200
Authorization: Basic YWRtaW46MTIzNA==
Content-Type: application/json

{ "fromAccountId": 5, "toAccountId": 7, "amount": 500, ... }
```

**Step 10 — Spring Security now checks authentication on the real request**

OPTIONS was `permitAll()`, but the real POST hits `.anyRequest().authenticated()`. Spring reads the `Authorization` header, verifies `admin:1234` → passes to `TransferController`.

---

### Visual Diagram

```
Browser (localhost:4200)              Spring Boot (localhost:8080)
         |                                       |
[AuthInterceptor adds Authorization header]      |
         |                                       |
[Browser sees: custom header + cross-origin]     |
[Browser auto-generates OPTIONS]                 |
         |                                       |
         |--- OPTIONS /api/v1/transfers -------> |
         |    Origin: http://localhost:4200       |
         |    Access-Control-Request-Method: POST |
         |    (no Authorization header)           |
         |                                       |
         |              [SpringSecurityConfig]    |
         |              OPTIONS → permitAll ✅    |
         |              CorsConfig: 4200 trusted ✅|
         |                                       |
         |<-- 200 OK (CORS approval headers) --- |
         |                                       |
[Browser reads approval — all checks pass]       |
         |                                       |
         |--- POST /api/v1/transfers ----------> |
         |    Authorization: Basic YWRtaW46MTIzNA==|
         |    Body: { transfer data }            |
         |                                       |
         |              [SpringSecurityConfig]    |
         |              anyRequest → authenticated|
         |              admin:1234 valid ✅       |
         |              → TransferController      |
         |                                       |
         |<-- 200 OK (TransferResponse) -------- |
```

---

### What Happens if CORS is Misconfigured?

| Problem | Result |
|---|---|
| `OPTIONS` not in `permitAll()` | Spring returns 401 on preflight → browser blocks all API calls |
| `localhost:4200` not in `allowedOrigins` | Preflight fails → browser blocks all API calls |
| `Authorization` not in `allowedHeaders` | Preflight fails → browser blocks all authenticated calls |
| `allowCredentials` not set to `true` | Cookies/auth headers stripped from cross-origin requests |

All four must be correctly configured for the app to work. The fact that your app works proves all four are correct.

---

### Why You See Two Requests in DevTools

When you open Chrome/Firefox DevTools → Network tab and make any API call, you see:
```
OPTIONS  /api/v1/transfers   200  (few ms)
POST     /api/v1/transfers   200  (normal response time)
```

The OPTIONS appears first in the actual network sequence (the browser blocks the POST until OPTIONS succeeds), but DevTools may display them slightly out of order visually depending on completion time. The OPTIONS request itself is so fast (no body, no auth processing) that its display position can look misleading.

You can always verify the true order by clicking each request in DevTools → Timing tab → the OPTIONS "Started" timestamp will always be earlier than the POST "Started" timestamp.

---

## 19. How Spring Security Authenticates Every Protected API Request

This section explains exactly what happens inside Spring Boot **every time** a protected endpoint receives a request. This is the mechanism behind `UserDetailsService` and HTTP Basic Auth.

---

### What is the Authorization Header?

When the `AuthInterceptor` in Angular adds `Authorization: Basic YWRtaW46MTIzNA==` to a request, that long string after `Basic ` is just `admin:1234` encoded in **Base64**.

Base64 is not encryption — it is just a way to convert text into a URL-safe string. Anyone can decode it:
```
YWRtaW46MTIzNA== → decoded → admin:1234
```

Spring Security decodes this automatically. The security comes from HTTPS (in production), not from Base64.

---

### What is `UserDetailsService`?

`UserDetailsService` is a Spring Security interface. It has one job: **given a username, return that user's stored details** (the stored password hash and roles).

Think of it as a **lookup table**:

```
Spring asks:  "Give me the details for username: admin"
              ↓
UserDetailsService looks through its list
              ↓
Returns:  { username: "admin", password: "$2a$10$abc...", roles: ["ADMIN"] }
```

In this project, the implementation is `InMemoryUserDetailsManager` — the list is stored **in RAM**, defined directly in `SpringSecurityConfig.java`:

```java
UserDetails admin = User.withUsername("admin")
        .password(encoder.encode("1234"))   // stores BCrypt hash, NOT "1234"
        .roles("ADMIN")
        .build();

return new InMemoryUserDetailsManager(admin);
```

Only one user exists in this list: `admin`. There is no database involved here — the list lives in memory as long as the Spring Boot app is running.

---

### Why is the Password Stored as a Hash, Not as "1234"?

`encoder.encode("1234")` runs BCrypt on `"1234"` and produces something like:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

This is a one-way hash — you cannot reverse it back to `"1234"`. Spring Security never compares plain text to plain text. Instead, it uses `BCryptPasswordEncoder.matches("1234", "$2a$10$N9qo8...")` which re-runs the BCrypt algorithm and checks if the result matches. This means even if someone stole the stored hash, they couldn't easily get the original password.

---

### The Full Authentication Flow — Step by Step

**Every protected request goes through this sequence:**

**Step 1 — Request arrives at Spring Boot**
```
POST /api/v1/transfers
Authorization: Basic YWRtaW46MTIzNA==
```

**Step 2 — Spring Security's Basic Auth filter activates**

Because `http.httpBasic(Customizer.withDefaults())` is configured in `SecurityFilterChain`, Spring has a filter sitting in front of every controller that intercepts requests needing authentication.

**Step 3 — Decode the Authorization header**
```
"Basic YWRtaW46MTIzNA==" 
→ strip "Basic " prefix
→ Base64 decode "YWRtaW46MTIzNA=="
→ "admin:1234"
→ split on ":"
→ username = "admin", password = "1234"
```

**Step 4 — Look up the user**
```java
userDetailsService.loadUserByUsername("admin")
// InMemoryUserDetailsManager searches its list for "admin"
// Returns the UserDetails object with the stored BCrypt hash
```

**Step 5 — Verify the password**
```java
passwordEncoder.matches("1234", "$2a$10$N9qo8...")
// BCrypt re-hashes "1234" and checks it matches the stored hash
// Returns: true ✅
```

**Step 6 — Decision**
- `true` → Spring marks the request as authenticated → passes to the controller
- `false` → Spring returns `401 Unauthorized` → controller never runs

---

### Visual Summary

```
Request arrives:
Authorization: Basic YWRtaW46MTIzNA==
           ↓
[Basic Auth Filter]
Decode Base64 → "admin:1234"
           ↓
[UserDetailsService.loadUserByUsername("admin")]
InMemoryUserDetailsManager returns:
  { username: "admin", passwordHash: "$2a$10$...", roles: ["ADMIN"] }
           ↓
[BCryptPasswordEncoder.matches("1234", "$2a$10$...")]
           ↓
        true ✅                    false ❌
           ↓                          ↓
  Request authenticated         401 Unauthorized
  → passes to controller        → controller never called
```

---

### Key Points to Remember

| Point | Detail |
|---|---|
| `UserDetailsService` stores | `admin` username + BCrypt hash of `"1234"` |
| Storage location | RAM (InMemoryUserDetailsManager) — no database |
| `Authorization` header | Base64-encoded `admin:1234` — not encrypted, just encoded |
| Password comparison | BCrypt.matches() — never compares plain text to plain text |
| Who creates this credential | It's defined at app startup in `SpringSecurityConfig.java` |
| Who uses this credential | The Angular `AuthInterceptor` sends `admin:1234` on every protected request |
| What happens on failure | 401 Unauthorized — Angular's `AuthInterceptor` catches this and calls `authService.logout()` |

---

## Exception Reference: Every Exception & Where It Is Handled

All custom exceptions live in `com.fidelity.mts.domain.exception`. Every one of them extends `RuntimeException` and is caught globally by `GlobalExceptionHandler` (`@ControllerAdvice`), which converts them into structured JSON error responses.

The response body shape for every error is:
```json
{
  "code": "ERR-CODE",
  "message": "Human readable message"
}
```

---

### 1. `InvalidCredentialsException`

| Property | Detail |
|---|---|
| **Error code** | `USR-401` |
| **HTTP status** | `401 Unauthorized` |
| **Message** | `"Invalid email or password."` |
| **Thrown in** | `UserServiceImpl.login()` |
| **When** | Email not found in DB **or** password does not match the stored BCrypt hash |
| **Backend handler** | `GlobalExceptionHandler.handleInvalidCredentialsException()` |
| **Frontend handler** | `StartPageComponent.onLogin()` error callback — displays `loginError` message in the login form |

**Throw sites:**
```java
// Email not found
userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new InvalidCredentialsException());

// Password mismatch
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new InvalidCredentialsException();
}
```

---

### 2. `DuplicateEmailException`

| Property | Detail |
|---|---|
| **Error code** | `USR-409` |
| **HTTP status** | `409 Conflict` |
| **Message** | `"An account with this email already exists."` |
| **Thrown in** | `UserServiceImpl.register()` |
| **When** | A user tries to register with an email that already exists in the `users` table |
| **Backend handler** | `GlobalExceptionHandler.handleDuplicateEmailException()` |
| **Frontend handler** | `StartPageComponent.onSignup()` error callback — checks `err.status === 409`, sets `isDuplicateEmail = true` and shows "Sign in instead →" prompt |

**Throw site:**
```java
if (userRepository.existsByEmail(request.getEmail())) {
    throw new DuplicateEmailException(request.getEmail());
}
```

---

### 3. `AccountNotFoundException`

| Property | Detail |
|---|---|
| **Error code** | `ACC-404` |
| **HTTP status** | `404 Not Found` |
| **Message** | `"Account not found with ID: {id}"` |
| **Thrown in** | `TransferServiceImpl.executeTransfer()` |
| **When** | The `fromAccountId` or `toAccountId` in a transfer request does not exist in the `accounts` table |
| **Backend handler** | `GlobalExceptionHandler.handleAccountNotFoundException()` |
| **Frontend handler** | `TransferComponent` — `showErrorToast(err.message)` displays a toast notification. Also caught by the `toAccountId` live-lookup `catchError` which shows `"Account not found"` inline below the recipient field |

**Throw sites:**
```java
Account fromAccount = accountRepository.findById(request.fromAccountId())
    .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));

Account toAccount = accountRepository.findById(request.toAccountId())
    .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));
```

---

### 4. `AccountNotActiveException`

| Property | Detail |
|---|---|
| **Error code** | `ACC-403` |
| **HTTP status** | `403 Forbidden` |
| **Message** | `"Account not active with ID: {id}"` |
| **Thrown in** | `TransferServiceImpl.executeTransfer()` |
| **When** | Either the sender or receiver account exists in the DB but has a non-`ACTIVE` status |
| **Backend handler** | `GlobalExceptionHandler.handleAccountNotActiveException()` |
| **Frontend handler** | `TransferComponent` — `showErrorToast(err.message)` displays a toast notification |

**Throw sites:**
```java
if (!fromAccount.isActive()) {
    throw new AccountNotActiveException(request.fromAccountId());
}
if (!toAccount.isActive()) {
    throw new AccountNotActiveException(request.toAccountId());
}
```

---

### 5. `InsufficientBalanceException`

| Property | Detail |
|---|---|
| **Error code** | `TRX-400` |
| **HTTP status** | `400 Bad Request` |
| **Message** | `"Insufficient balance in account {id}. Attempted: {amount}, Available: {balance}"` |
| **Thrown in** | `Account.debit()` (domain model method) |
| **When** | The sender's balance is less than the transfer amount |
| **Backend handler** | `GlobalExceptionHandler.handleInsufficientBalanceException()` |
| **Frontend handler** | `TransferComponent` — `showErrorToast(err.message)` displays a toast notification |

**Note:** This exception is thrown inside the domain model's `debit()` method, not directly in `TransferServiceImpl`. It propagates up through `executeTransfer()`, is caught by the local `catch (Exception e)` block which saves a `FAILED` transaction log, then re-thrown so `GlobalExceptionHandler` converts it to a 400 response.

---

### 6. `InvalidTransferException`

| Property | Detail |
|---|---|
| **Error code** | `VAL-422` |
| **HTTP status** | `422 Unprocessable Entity` |
| **Message** | `"Illegal Transfer operation : Sender and receiver accounts cannot be the same."` |
| **Thrown in** | `TransferServiceImpl.validateTransfer()` and `TransferServiceImpl.executeTransfer()` |
| **When** | (1) `fromAccountId == toAccountId` (self-transfer) **or** (2) `amount <= 0` |
| **Backend handler** | `GlobalExceptionHandler.handleInvalidTransferException()` |
| **Frontend handler** | `TransferComponent` — the UI pre-validates self-transfer (`id === this.authService.getAccountId()`) and shows `"Cannot transfer to yourself"` inline before the request is even made. If it somehow reaches the backend, `showErrorToast(err.message)` is shown |

**Throw sites:**
```java
// validateTransfer()
if (request.fromAccountId() == request.toAccountId()) throw new InvalidTransferException();
if (request.amount().compareTo(BigDecimal.ZERO) <= 0)  throw new InvalidTransferException();

// executeTransfer() — second redundant guard
if (request.fromAccountId() == request.toAccountId()) throw new InvalidTransferException();
```

---

### 7. `DuplicateTransferException`

| Property | Detail |
|---|---|
| **Error code** | `TRX-409` |
| **HTTP status** | `409 Conflict` |
| **Message** | `"Duplicate transfer detected with ID: {uuid}"` |
| **Thrown in** | `TransferServiceImpl.checkIdempotency()` |
| **When** | A transfer request arrives with a `idempotencyKey` (UUID) that already exists in the `transaction_logs` table — prevents accidental double submissions |
| **Backend handler** | `GlobalExceptionHandler.handleDuplicateTransferException()` |
| **Frontend handler** | `TransferComponent` — `showErrorToast(err.message)` displays a toast notification |

**Throw site:**
```java
transactionLogRepository.findByIdempotencyKey(idempotencyKey)
    .ifPresent(log -> {
        throw new DuplicateTransferException(idempotencyKey);
    });
```

---

### 8. Unhandled `RuntimeException` — Linked account not found

| Property | Detail |
|---|---|
| **Error code** | *(none — not a custom exception)* |
| **HTTP status** | `500 Internal Server Error` (Spring default) |
| **Message** | `"Linked account not found."` |
| **Thrown in** | `UserServiceImpl.login()` |
| **When** | A user record exists in the `users` table but their `accountId` foreign key points to a non-existent account. This should never happen due to the `@Transactional` registration flow that always creates both together |
| **Backend handler** | No custom `@ExceptionHandler` — falls through to Spring's default 500 handler |
| **Frontend handler** | `StartPageComponent.onLogin()` error callback — displays the generic `"Invalid email or password."` fallback |

**Throw site:**
```java
Account account = accountRepository.findById(user.getAccountId())
    .orElseThrow(() -> new RuntimeException("Linked account not found."));
```

---

### 9. Frontend-only: Spring Security `401 Unauthorized` (wrong admin credentials)

| Property | Detail |
|---|---|
| **Not a Java exception** | Returned directly by Spring Security's filter chain before the controller is called |
| **HTTP status** | `401 Unauthorized` |
| **When** | The `Authorization: Basic ...` header on a protected request carries invalid or missing `admin:1234` credentials |
| **Frontend handler** | `AuthInterceptor.intercept()` — `catchError` block checks `error.status === 401 && !isPublicUserEndpoint` → calls `authService.logout()` → redirects to `/start` |

```typescript
catchError((error: HttpErrorResponse) => {
  if (error.status === 401 && !isPublicUserEndpoint) {
    this.authService.logout(); // clears localStorage, navigates to /start
  }
  return throwError(() => error);
})
```

---

### Summary Table

| Exception | Code | HTTP | Thrown By | Frontend Shown As |
|---|---|---|---|---|
| `InvalidCredentialsException` | `USR-401` | 401 | `UserServiceImpl.login()` | Login form error message |
| `DuplicateEmailException` | `USR-409` | 409 | `UserServiceImpl.register()` | "Sign in instead →" prompt |
| `AccountNotFoundException` | `ACC-404` | 404 | `TransferServiceImpl.executeTransfer()` | Error toast / inline "Account not found" |
| `AccountNotActiveException` | `ACC-403` | 403 | `TransferServiceImpl.executeTransfer()` | Error toast |
| `InsufficientBalanceException` | `TRX-400` | 400 | `Account.debit()` (domain model) | Error toast |
| `InvalidTransferException` | `VAL-422` | 422 | `TransferServiceImpl.validateTransfer()` | Error toast (pre-validated in UI) |
| `DuplicateTransferException` | `TRX-409` | 409 | `TransferServiceImpl.checkIdempotency()` | Error toast |
| `RuntimeException` (orphaned account) | *(none)* | 500 | `UserServiceImpl.login()` | Generic "Invalid email or password." |
| Spring Security 401 | *(none)* | 401 | Spring Security filter chain | `authService.logout()` → redirect to `/start` |
