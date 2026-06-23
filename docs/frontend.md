# Frontend Documentation — Money Transfer System

> **Audience:** This document is written for beginner full-stack developers. Every file in the Angular frontend is explained in detail — what it does, why it exists, when it is used, and how it connects to the rest of the system. Read it top-to-bottom for the first time; use it as a reference later.

---

## What Is the Frontend?

The frontend is the **visual, user-facing part** of the application. It runs in the user's web browser. The user never directly touches the backend (Spring Boot server) — they only interact with the frontend UI, which in turn communicates with the backend through HTTP API calls.

In this project, the frontend is built with **Angular**, a powerful JavaScript/TypeScript framework created by Google. Angular makes it easy to build complex single-page applications (SPAs).

### What is a Single-Page Application (SPA)?
A traditional website loads a new HTML page from the server every time you click a link. In an SPA, only one HTML page is ever loaded. Navigation between "pages" (e.g., Dashboard → Transfer → History) is handled entirely by JavaScript — the page never fully reloads. This makes the app feel fast and smooth.

### Technology Stack
| Technology | Role |
|---|---|
| **Angular 19** | Frontend framework — component system, routing, dependency injection |
| **TypeScript** | Strongly-typed superset of JavaScript (compiles to JavaScript) |
| **HTML templates** | Define the structure of each page |
| **CSS** | Style each component |
| **RxJS** | Library for handling asynchronous operations (HTTP calls, events) using Observables |
| **Reactive Forms** | Angular's form library with validation support |
| **HttpClient** | Angular's built-in HTTP client for making API calls |

---

## Project Directory Structure

```
frontend/mts-ui/
├── package.json                   ← Node.js dependencies list
├── angular.json                   ← Angular CLI configuration
├── tsconfig.json                  ← TypeScript compiler configuration
└── src/
    ├── index.html                 ← The single HTML page (the "shell")
    ├── main.ts                    ← Bootstrap file (starts Angular)
    ├── main.server.ts             ← Server-side rendering bootstrap
    ├── server.ts                  ← Express server for SSR
    ├── styles.css                 ← Global CSS styles
    └── app/
        ├── app.ts                 ← Root component
        ├── app.html               ← Root component template
        ├── app.css                ← Root component styles
        ├── app-module.ts          ← Root NgModule (app configuration)
        ├── app-routing-module.ts  ← URL routing configuration
        ├── app.module.server.ts   ← Server-side module
        ├── app.routes.server.ts   ← Server-side routes
        ├── app.spec.ts            ← Tests for root component
        │
        ├── components/            ← UI components (one folder per screen)
        │   ├── start-page-component/    ← Login & Register page
        │   ├── dashboard-component/     ← Account overview
        │   ├── transfer-component/      ← Send money
        │   ├── history-component/       ← Transaction history
        │   ├── profile-component/       ← Account profile view
        │   ├── reward-component/        ← Reward points
        │   ├── login-component/         ← Admin login (legacy)
        │   └── top-navbar-component/    ← Navigation bar
        │
        ├── service/               ← HTTP services (talk to backend)
        │   ├── auth-service.ts          ← Manages login state & tokens
        │   ├── user-service.ts          ← Register/Login API calls
        │   ├── account-service.ts       ← Account data API calls
        │   ├── transfer-service.ts      ← Transfer money API calls
        │   └── reward-service.ts        ← Reward history API calls
        │
        ├── interceptors/          ← Middleware that modifies all HTTP requests
        │   └── auth-interceptor.ts      ← Adds Authorization header
        │
        ├── guards/                ← Route protection (prevent unauthorized access)
        │   └── auth-guard.ts            ← Blocks access if not logged in
        │
        ├── models/                ← TypeScript interfaces (data shapes)
        │   ├── account.ts
        │   ├── transaction-log.ts
        │   ├── transfer-request.ts
        │   ├── transfer-response.ts
        │   └── reward.ts
        │
        └── enums/                 ← TypeScript enumerations
            ├── AccountStatus.ts
            └── TransactionStatus.ts
```

---

## Core Concepts Before Reading Files

### What is a Component?
A component is a **reusable piece of UI**. Every "screen" in this app is a component. Each component consists of:
- **`.ts` file** — TypeScript class with the logic
- **`.html` file** — Template defining what is displayed
- **`.css` file** — Styles specific to this component

### What is a Service?
A service is a class that holds **shared logic or data** — like making HTTP calls. Services are **not tied to a specific component**. Multiple components can inject and use the same service.

### What is Dependency Injection (DI)?
Angular's DI system is like a vending machine. You declare what you need (e.g., `private authService: AuthService`), and Angular automatically gives you the right object — you don't create it yourself with `new`. This makes code cleaner and testing much easier.

### What is an Observable?
Angular uses RxJS Observables for asynchronous operations (especially HTTP calls). Think of an Observable as a "promise on steroids". When you call `http.get(...)`, you don't get the data immediately. You get an Observable that you **subscribe** to:

```typescript
this.accountService.getAccount(id).subscribe({
  next: (data) => { /* success — data is available here */ },
  error: (err) => { /* something went wrong */ }
});
```

---

## Configuration Files

---

### `package.json` — Node.js Dependencies

**What is it?**
Similar to `pom.xml` in the backend. It lists all JavaScript libraries (packages) the project depends on and the scripts to run the project.

**Key scripts:**
```json
"scripts": {
  "start": "ng serve",         ← Run the development server (localhost:4200)
  "build": "ng build"          ← Build for production (outputs to dist/ folder)
}
```

**Key dependencies:**
| Package | Purpose |
|---|---|
| `@angular/core` | Core Angular framework |
| `@angular/common` | Common Angular directives (`NgIf`, `NgFor`, `DatePipe`) |
| `@angular/router` | Client-side routing |
| `@angular/forms` | Reactive and Template-driven forms |
| `@angular/common/http` | HttpClient for API calls |
| `rxjs` | Reactive programming library (Observables) |

**When is it used?**
Run `npm install` once to download all packages into `node_modules/`. The `node_modules/` folder is gitignored (not tracked in Git) because it's very large — each developer runs `npm install` themselves.

---

### `angular.json` — Angular CLI Configuration

**What is it?**
Configuration for the Angular CLI build tool. Defines:
- Where the main entry file is (`src/main.ts`)
- Where the global styles file is (`src/styles.css`)
- Build optimization settings
- Development server settings

You rarely need to edit this file directly.

---

### `tsconfig.json` — TypeScript Configuration

**What is it?**
Tells the TypeScript compiler how to process `.ts` files.

**Key settings for beginners:**
```json
{
  "strict": true,           ← Enables strict type checking — helps catch bugs early
  "target": "ES2022",       ← Compiles TypeScript to ES2022 JavaScript
  "module": "ES2022"        ← Uses ES modules (import/export syntax)
}
```

**Why TypeScript instead of plain JavaScript?**
TypeScript adds **static types** to JavaScript. Instead of discovering a bug at runtime (when a user is using the app), TypeScript catches it at compile time (when you write the code). For example:

```typescript
// JavaScript (no types) — this bug appears at runtime
function greet(name) { return "Hello " + name.toUpperCase(); }
greet(42); // Runtime error: name.toUpperCase is not a function

// TypeScript — caught immediately by the compiler
function greet(name: string) { return "Hello " + name.toUpperCase(); }
greet(42); // Compile-time error: Argument of type 'number' is not assignable to 'string'
```

---

### `src/index.html` — The Shell HTML Page

**What is it?**
The **single HTML file** that the browser loads. Every Angular SPA has exactly one `index.html`.

```html
<body>
  <app-root></app-root>
</body>
```

`<app-root>` is the selector for the root `App` component. Angular replaces this tag with the actual component content at runtime.

**Why just one HTML file?**
In an SPA, Angular itself handles "navigation" by swapping out components dynamically inside `<app-root>`. The browser never loads a new page — Angular just changes what's rendered inside the single page.

---

### `src/main.ts` — Angular Bootstrap

**What is it?**
The first TypeScript file that runs when the browser loads the app. It bootstraps (starts) the Angular application.

```typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { AppModule } from './app/app-module';

bootstrapApplication(AppModule);
```

**When is it used?**
Once — at application startup. After Angular initializes, it takes over and manages everything else.

---

### `src/styles.css` — Global Styles

**What is it?**
CSS rules applied to the entire application, not just one component. For example, `body { margin: 0; }` or font imports. Component-specific styles go in the component's own `.css` file.

---

### `src/main.server.ts` and `src/server.ts` — Server-Side Rendering (SSR)

**What is it?**
Angular Universal / SSR (Server-Side Rendering) allows the Angular app to run on a Node.js server (using Express) and send fully-rendered HTML to the browser instead of an empty shell.

- `main.server.ts` — bootstraps Angular in the server context
- `server.ts` — sets up an Express HTTP server that handles SSR

**Why SSR?**
- Better SEO (search engines can read pre-rendered content)
- Faster first page load (browser shows content before JavaScript runs)

**Is it critical for this project?**
For a learning/demo project, SSR is optional. The app works fine without it. It's included as a best-practice feature.

---

## `app/` — The Application Root

---

### `app-module.ts` — Root Angular Module

**What is it?**
The **root module** that pulls everything together and tells Angular how to set up the application. Think of it as the app's "configuration center."

```typescript
@NgModule({
  declarations: [
    App,                   // Root component
    TopNavbarComponent,    // Navbar
    LoginComponent,        // Admin login (legacy)
    StartPageComponent,    // Login/Register page
    DashboardComponent,    // Account overview
    HistoryComponent,      // Transaction history
    ProfileComponent,      // Account profile
  ],
  imports: [
    BrowserModule,         // Core browser capabilities
    AppRoutingModule,      // URL routing
    ReactiveFormsModule,   // Forms with validation
    CommonModule,          // NgIf, NgFor, DatePipe, etc.
    RouterModule,          // RouterLink, RouterOutlet directives
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),     // Global error handling
    provideClientHydration(withEventReplay()), // SSR hydration
    provideHttpClient(withFetch(), withInterceptorsFromDi()), // HTTP client + interceptors
    {
      provide: HTTP_INTERCEPTORS,    // Register the auth interceptor
      useClass: AuthInterceptor,
      multi: true,                   // "multi: true" = allow multiple interceptors
    },
  ],
  bootstrap: [App],  // Which component to render first
})
export class AppModule {}
```

**Key concepts:**

**`declarations`** — Which components, directives, and pipes belong to this module. A component must be declared in exactly one module to be used.

**`imports`** — Other modules whose features you want to use. `ReactiveFormsModule` adds support for `FormGroup`, `FormControl`, etc.

**`providers`** — Services and configurations available throughout the app.
- `provideHttpClient(withInterceptorsFromDi())` — Enables `HttpClient` and tells it to use DI-provided interceptors (like our `AuthInterceptor`).
- The `HTTP_INTERCEPTORS` provider registers `AuthInterceptor` as a middleware for all HTTP requests.

**`bootstrap`** — The first component Angular renders when the app starts.

**Note:** `TransferComponent` and `RewardComponent` are **standalone components** (they declare their own imports) and are lazy-loaded via the router — that's why they don't appear in `declarations`.

---

### `app-routing-module.ts` — URL Routing

**What is it?**
Defines the mapping from **URL paths** to **components**. When the user navigates to `/dashboard`, Angular knows to show `DashboardComponent`.

```typescript
const routes: Routes = [
  // ── Public routes (no login required) ────
  { path: 'start',       component: StartPageComponent },    // Login/Register
  { path: 'admin-login', component: LoginComponent },        // Legacy admin login
  { path: 'login',       redirectTo: 'start', pathMatch: 'full' }, // Old URL redirect

  // ── Protected routes (login required) ────
  { path: 'transfer', component: TransferComponent,  canActivate: [AuthGuard] },
  { path: 'history',  component: HistoryComponent,   canActivate: [AuthGuard] },
  { path: 'profile',  component: ProfileComponent,   canActivate: [AuthGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  {
    path: 'rewards',
    loadComponent: () => import('./components/reward-component/reward-component')
                           .then(m => m.RewardComponent), // Lazy loading
    canActivate: [AuthGuard]
  },

  // ── Default routes ─────────────────────────
  { path: '',   redirectTo: 'start', pathMatch: 'full' }, // Empty URL → /start
  { path: '**', redirectTo: 'start' },                    // Unknown URL → /start
];
```

**`canActivate: [AuthGuard]`** — Before allowing navigation to this route, Angular calls `AuthGuard.canActivate()`. If the user is not logged in, they are redirected to `/start`.

**Lazy Loading (`loadComponent`):**
For the `rewards` route, the component is not loaded upfront. Instead, it is downloaded from the server only when the user navigates to `/rewards`. This reduces the initial bundle size and makes the app load faster. This is called **code splitting**.

**`pathMatch: 'full'`** — The redirect only triggers when the path is exactly `""` (empty), not when it's a prefix of something else.

**`'**'` (wildcard)** — Catches any URL that doesn't match any defined route (e.g., user types a wrong URL). Redirects to `/start`.

---

### `app.ts` — Root Component

**What is it?**
The top-level component. It's the container for everything else.

```typescript
@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  standalone: false,
})
export class App {}
```

**`app.html` contains:**
```html
<router-outlet></router-outlet>
```

`<router-outlet>` is where Angular renders the current route's component. When the user navigates to `/dashboard`, Angular puts `DashboardComponent` here. It's the "stage" for the entire application.

---

## Guards — Route Protection

---

### `guards/auth-guard.ts` — Authentication Guard

**What is it?**
A guard that **protects routes** from being accessed by unauthenticated users.

```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      return true;                      // Allow navigation
    }
    this.router.navigate(['/start']);   // Redirect to login
    return false;                       // Block navigation
  }
}
```

**How it works:**
1. Angular checks if a route has `canActivate: [AuthGuard]`
2. Before rendering the component, it calls `authGuard.canActivate()`
3. `canActivate()` checks `authService.isLoggedIn()` (which checks if a token exists in localStorage)
4. If logged in → return `true` (proceed to route)
5. If not logged in → redirect to `/start` and return `false` (block route)

**When is it used?**
Every time the user tries to navigate to a protected route like `/dashboard`, `/transfer`, `/history`, `/profile`, or `/rewards`.

**Why do we need this?**
Without the guard, a user could type `http://localhost:4200/dashboard` in the browser and access the dashboard without being logged in. The guard prevents this.

---

## Interceptors — HTTP Middleware

---

### `interceptors/auth-interceptor.ts` — Authorization Interceptor

**What is it?**
An interceptor is **middleware for HTTP requests**. It sits between the Angular `HttpClient` and the actual network call. Every HTTP request the app makes passes through the interceptor.

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // Public endpoints (register/login) must NOT carry auth header
    const isPublicUserEndpoint = req.url.includes('/api/v1/users/');

    let authReq = req;
    if (token && !isPublicUserEndpoint) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Basic ${token}`,
        },
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Auto-logout if a PROTECTED endpoint returns 401
        if (error.status === 401 && !isPublicUserEndpoint) {
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
```

**Breaking down the `intercept` method:**

1. **`req`** — The outgoing HTTP request object
2. **`next`** — The next handler in the chain (ultimately, the actual network call)
3. `req.clone({ setHeaders: { Authorization: ... } })` — HTTP requests are **immutable** in Angular. You can't modify a request; you have to create a clone with the changes.
4. **Why skip auth for `/api/v1/users/`?** — The register and login endpoints don't require authentication. If the user had a stale (expired or invalid) token in localStorage, attaching it to the login request would cause Spring Security to reject it with a 401 before the login endpoint is even reached.
5. **Auto-logout on 401** — If a protected endpoint returns 401 (Unauthorized), it means the token is invalid or expired. The interceptor automatically logs the user out and redirects them to the login page.

**When is it used?**
Automatically, for **every single HTTP call** made anywhere in the application. You don't need to manually add the Authorization header in each service — the interceptor handles it globally.

**Registration in `app-module.ts`:**
```typescript
{
  provide: HTTP_INTERCEPTORS,
  useClass: AuthInterceptor,
  multi: true,
}
```
`multi: true` allows multiple interceptors to be registered. The interceptors form a chain — each one can modify the request, pass it to the next, and optionally handle the response.

---

## Services — Backend Communication

---

### `service/auth-service.ts` — Authentication Service

**What is it?**
The most important service in the frontend. It manages **authentication state** — whether the user is logged in, their identity, and their session token.

**Where is the data stored?**
In `localStorage` — a browser API that persists data even after the browser tab is closed.

```typescript
private readonly TOKEN_KEY = 'auth_token';
private readonly USER_KEY = 'auth_user';
private readonly ACCOUNT_ID_KEY = 'account_id';
```

**The `login()` method (used by the admin login form):**
```typescript
login(username: string, password: string, accountId: number): void {
  const token = btoa(`${username}:${password}`); // Base64 encode "admin:1234"
  localStorage.setItem(this.TOKEN_KEY, token);
  localStorage.setItem(this.USER_KEY, username);
  localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
  this.loggedIn.next(true);
}
```
`btoa()` is a built-in browser function for Base64 encoding. `btoa('admin:1234')` → `'YWRtaW46MTIzNA=='`. This is the HTTP Basic Auth token.

**The `setUserSession()` method (used after successful user login):**
```typescript
setUserSession(holderName: string, accountId: number): void {
  const token = btoa('admin:1234'); // Always use admin credentials for backend auth
  localStorage.setItem(this.TOKEN_KEY, token);
  localStorage.setItem(this.USER_KEY, holderName);
  localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
  this.loggedIn.next(true);
}
```

**Why always use `admin:1234` for the backend token?**
This is the design choice in this project. The backend has one admin user (`admin:1234`) that protects all routes via HTTP Basic Auth. The frontend validates user credentials through the user-specific `/api/v1/users/login` endpoint (checking against the MySQL database). After that validation, the session uses the shared admin token for all subsequent backend API calls.

This is a **simplified authentication model for learning purposes**. In production, you'd use JWT tokens tied to the individual user.

**The `logout()` method:**
```typescript
logout(): void {
  localStorage.removeItem(this.TOKEN_KEY);
  localStorage.removeItem(this.USER_KEY);
  localStorage.removeItem(this.ACCOUNT_ID_KEY);
  localStorage.removeItem(this.USER_ROLE_KEY);
  this.loggedIn.next(false);
  this.router.navigate(['/start']);
}
```
Clears all stored data and redirects to the login page.

**`BehaviorSubject<boolean>` for reactive login state:**
```typescript
private loggedIn: BehaviorSubject<boolean>;
isLoggedIn$;   // Observable that components can subscribe to
```
A `BehaviorSubject` is a special RxJS Observable that:
- Holds the **current value** (true/false for login state)
- Immediately emits the current value to any new subscriber
- Emits a new value whenever `loggedIn.next(...)` is called

The `isLoggedIn$` observable allows components to react to login/logout events in real time (e.g., the navbar could show/hide a login button based on this).

**`isPlatformBrowser` check:**
```typescript
this.isBrowser = isPlatformBrowser(platformId);
```
For SSR (server-side rendering), `localStorage` does not exist on the Node.js server. This check prevents crashes when the code runs on the server.

---

### `service/user-service.ts` — User Registration & Login Service

**What is it?**
Makes API calls to the **public** user endpoints for registration and login.

**Interfaces defined in this file:**
```typescript
export interface UserRegistrationRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserLoginRequest {
  email: string;
  password: string;
}

export interface UserLoginResponse {
  accountId: number;
  holderName: string;
  message: string;
}
```
These TypeScript interfaces mirror the Java DTO classes in the backend. They ensure the data you send and receive has the correct shape — TypeScript will flag a type error if you try to access a field that doesn't exist.

**Methods:**
```typescript
register(request: UserRegistrationRequest): Observable<{ message: string }> {
  return this.http.post<{ message: string }>(`${this.BASE_URL}/register`, request);
}

login(request: UserLoginRequest): Observable<UserLoginResponse> {
  return this.http.post<UserLoginResponse>(`${this.BASE_URL}/login`, request);
}
```

**Why does `register()` not need the auth interceptor?**
`BASE_URL = 'http://localhost:8080/api/v1/users'`
The interceptor checks: `req.url.includes('/api/v1/users/')` → if true, it skips adding the Authorization header. This ensures register/login calls are truly public.

---

### `service/account-service.ts` — Account Data Service

**What is it?**
Makes authenticated API calls to get account information.

```typescript
private URL = "http://localhost:8080/api/v1/accounts"
```

**Methods:**

`getAccount(id: number)` → `GET /api/v1/accounts/{id}`
- Returns `Observable<Account>`
- Used by: Dashboard, Profile, and Transfer components

`getAccountBalance(id: number)` → `GET /api/v1/accounts/{id}/balance`
- Returns `Observable<number>` (just the balance)

`getAccountTransactions(id: number)` → `GET /api/v1/accounts/{id}/transactions`
- Returns `Observable<TransactionLog[]>`
- Used by: History component

`refreshAccount(id: number)` → Same as `getAccount` but clears the cache first
- Used by: Transfer component after a successful transfer, to force the dashboard to show the updated balance

**Cache fields (currently not fully used):**
```typescript
private accountCache = new Map<number, Observable<Account>>();
private accountDataCache = new Map<number, Account>();
```
These fields exist for a potential caching optimization — to avoid re-fetching the same account multiple times. `refreshAccount()` clears these caches. Currently, `getAccount()` doesn't use the cache — it always makes a fresh API call.

---

### `service/transfer-service.ts` — Transfer Service

**What is it?**
Makes the API call to initiate a money transfer.

```typescript
private apiUrl = 'http://localhost:8080/api/v1/transfers'
```

**The `transfer()` method:**
```typescript
transfer(request: TransferRequest): Observable<TransferResponse> {
  return this.http.post<TransferResponse>(this.apiUrl, request).pipe(
    catchError(this.handleError)
  )
}
```

`pipe(catchError(...))` — RxJS operator that catches errors and transforms them. Instead of letting the raw HTTP error bubble up to the component, `handleError` translates it into a user-friendly message.

**Error handling (`handleError`):**
```typescript
private handleError(error: HttpErrorResponse) {
  if (error.error instanceof ErrorEvent) {
    // Client-side error (network failure, etc.)
    errorMessage = `Error: ${error.error.message}`;
  } else {
    // Server-side error — map status codes to messages
    if (error.status === 403) errorMessage = 'You are not authorized...';
    if (error.status === 400) errorMessage = error.error?.message || 'Invalid request';
    if (error.status === 404) errorMessage = 'Account not found.';
    else errorMessage = error.error?.message || `Server error: ${error.status}`;
  }
  return throwError(() => new Error(errorMessage));
}
```

The backend's `GlobalExceptionHandler` returns structured JSON errors like:
```json
{ "error": "TRX-400", "message": "Insufficient balance in account 42..." }
```
`error.error?.message` accesses the `message` field from this JSON body.

---

### `service/reward-service.ts` — Reward Points Service

**What is it?**
Makes the API call to fetch reward history.

```typescript
private readonly url = 'http://localhost:8080/api/v1/accounts/rewards';

getRewards(accountId: number): Observable<Reward[]> {
  return this.http.get<Reward[]>(`${this.url}/${accountId}`)
    .pipe(catchError(this.handleError));
}
```

`GET /api/v1/accounts/rewards/{accountId}` → returns `List<RewardLedger>` from backend → received as `Reward[]` in frontend.

---

## Models — TypeScript Data Shapes

Models are TypeScript **interfaces** that define the shape of data objects. They don't contain any logic — just field definitions. They are the frontend's equivalent of the backend's DTO/Entity classes.

---

### `models/account.ts`
```typescript
import { AccountStatus } from "../enums/AccountStatus";

export interface Account {
  id: number;
  holderName: string;
  balance: number;
  status: AccountStatus;  // 'ACTIVE' | 'LOCKED' | 'CLOSED'
  lastUpdated: Date;
}
```
Mirrors `AccountResponse.java` from the backend.

---

### `models/transaction-log.ts`
```typescript
import { TransactionStatus } from '../enums/TransactionStatus';

export interface TransactionLog {
  id: string;                   // UUID
  fromAccountId: number;
  toAccountId: number;
  fromAccountName: string;
  toAccountName: string;
  amount: number;
  status: TransactionStatus;    // 'SUCCESS' | 'FAILED'
  failureReason?: string;       // Optional — only present if FAILED
  idempotencyKey: string;
  createdOn: Date;
}
```
`?` on `failureReason` means it is optional — TypeScript won't require it to always be present, matching the backend where `failureReason` is NULL for successful transfers.

---

### `models/transfer-request.ts`
```typescript
export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey: string;   // UUID string
}
```
What the `TransferComponent` sends to the backend.

---

### `models/transfer-response.ts`
```typescript
export interface TransferResponse {
  transactionId: string;   // "TRX-<UUID>"
  status: string;          // 'SUCCESS' or 'FAILED'
  message: string;
  debitedFrom: number;
  creditedTo: number;
  amount: number;
}
```
What the backend returns after a transfer attempt.

---

### `models/reward.ts`
```typescript
export interface Reward {
  id: number;
  accountId: number;
  transactionId: string;    // UUID of the triggering transaction
  pointsAwarded: number;
  description: string;
  createdAt: string;        // ISO datetime string
}
```
Mirrors `RewardLedger.java` from the backend.

---

## Enums — TypeScript Enumerations

---

### `enums/AccountStatus.ts`
```typescript
export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  LOCKED = 'LOCKED',
  CLOSED = 'CLOSED'
}
```
Mirrors `Enums.AccountStatus` from the backend. Using an enum here ensures you can't accidentally type `'acitve'` (typo) — TypeScript will flag it.

---

### `enums/TransactionStatus.ts`
```typescript
export enum TransactionStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}
```
Used in `TransactionLog` interface and in `HistoryComponent` to filter transactions by status.

---

## Components — User Interface Screens

Each component folder contains:
- `.ts` — The class with logic
- `.html` — The template (what's rendered)
- `.css` — Component-specific styles
- `.spec.ts` — Unit test file

---

### `components/start-page-component/` — Login & Registration Page (Main Entry Point)

**Route:** `/start` (or `/` which redirects here)

**What is it?**
The main entry screen for all users. It has **two tabs**: Login and Sign Up. This is the `StartPageComponent`.

**`start-page-component.ts` explained:**

```typescript
export class StartPageComponent {
  activeTab: 'login' | 'signup' = 'login';  // Which tab is showing

  loginForm: FormGroup;
  signupForm: FormGroup;
```

**Reactive Forms Setup:**
```typescript
this.loginForm = this.fb.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', Validators.required],
});

this.signupForm = this.fb.group(
  {
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
  },
  { validators: passwordMatchValidator }  // Cross-field validation
);
```

`FormGroup` is a container for `FormControl` objects. Each `FormControl` holds the current value and validation state of one form field. `Validators.required`, `Validators.email`, `Validators.minLength(6)` are built-in validators.

**`passwordMatchValidator` (cross-field validation):**
```typescript
const passwordMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const pw = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
};
```
This validator runs on the entire group (not just one field) and checks if `password` equals `confirmPassword`. If they don't match, it returns `{ passwordMismatch: true }`. The template can then display an error.

**`onLogin()` — What happens when the user clicks "Sign In":**
```typescript
onLogin(): void {
  if (this.loginForm.invalid) return;  // Don't submit if form has errors
  this.isLoading = true;

  const { email, password } = this.loginForm.value;
  this.userService.login({ email, password }).subscribe({
    next: (res: UserLoginResponse) => {
      // 1. Store the session (admin Basic Auth token + accountId + holderName)
      this.authService.setUserSession(res.holderName, res.accountId);
      // 2. Navigate to dashboard
      this.router.navigate(['/dashboard']);
    },
    error: (err) => {
      // Show error message extracted from the HTTP error response
      this.loginError = err.error?.message || 'Invalid email or password.';
    },
  });
}
```

**`onSignup()` — What happens when the user clicks "Register":**
```typescript
onSignup(): void {
  if (this.signupForm.invalid) return;
  const { name, email, password } = this.signupForm.value;

  this.userService.register({ name, email, password }).subscribe({
    next: (res) => {
      this.signupSuccess = res.message + ' You can now sign in.';
      this.signupForm.reset();
      // After 2 seconds, automatically switch to the login tab
      setTimeout(() => { this.setTab('login'); }, 2000);
    },
    error: (err) => {
      if (err?.status === 409) {
        this.isDuplicateEmail = true;  // Show "email already exists" error
      }
      this.signupError = errorMsg;
    },
  });
}
```

**`ChangeDetectorRef` and `cdr.detectChanges()`:**
By default, Angular checks if the view needs updating during certain lifecycle events (called Change Detection). However, async callbacks (like `.subscribe()` results) sometimes don't trigger it automatically. Calling `this.cdr.detectChanges()` manually forces Angular to re-render the component. This is needed here to immediately show error/success messages.

---

### `components/dashboard-component/` — Account Dashboard

**Route:** `/dashboard`

**What is it?**
The home screen after login. Displays the user's account balance, holder name, and account status. Provides navigation to Transfer, History, Profile, and Rewards.

**`dashboard-component.ts` explained:**

```typescript
public account = signal<Account>({
  id: 0, holderName: '', balance: 0, status: AccountStatus.ACTIVE, lastUpdated: new Date()
});
```

`signal<Account>()` is a new Angular feature (introduced in Angular 16+). It's a reactive state container. When you call `account.set(newValue)`, Angular automatically re-renders any part of the template that reads `account()`.

**`ngOnInit()`** — Called once when the component is first displayed:
```typescript
ngOnInit(): void {
  this.loadAccount();
}

loadAccount() {
  const accId = this.authService.getAccountId() ?? 0;  // Get account ID from localStorage
  this.accountService.getAccount(accId).subscribe((data) => {
    if (data) this.account.set(data);  // Update the signal → triggers re-render
  });
}
```

**`ChangeDetectionStrategy.OnPush`:**
```typescript
changeDetection: ChangeDetectionStrategy.OnPush
```
A performance optimization. With `OnPush`, Angular only checks this component for updates when:
1. An input property changes
2. A signal it reads changes
3. An Observable it subscribes to emits
4. `markForCheck()` is called manually

This reduces unnecessary re-rendering and makes the app more efficient.

---

### `components/transfer-component/` — Money Transfer Screen

**Route:** `/transfer`

**What is it?**
The most complex component. Allows the user to enter a recipient account ID and amount, validates in real time, and submits the transfer.

**Key features:**
- Auto-prefills the "From Account" field with the logged-in user's account
- Live recipient name lookup as the user types the recipient's account ID
- Idempotency key generation
- Error toast notification
- Auto-redirect to dashboard after successful transfer

**`transfer-component.ts` explained:**

**Form setup:**
```typescript
this.transferForm = this.fb.group({
  fromAccountId: [{ value: displayDetails, disabled: true }],  // Read-only — pre-filled
  toAccountId: ['', Validators.required],
  amount: ['', [Validators.required, Validators.min(0.01)]],
});
```

**Live recipient name lookup:**
```typescript
this.toAccountSub = this.transferForm.get('toAccountId')?.valueChanges.pipe(
  debounceTime(400),          // Wait 400ms after the user stops typing before making API call
  distinctUntilChanged(),     // Don't call API if the value didn't change
  switchMap(val => {
    const id = Number(val);
    if (id && !isNaN(id) && id > 0) {
      if (id === this.authService.getAccountId()) {
        this.recipientName = 'Cannot transfer to yourself';
        return of(null);       // Don't make API call
      }
      return this.accountService.getAccount(id).pipe(
        catchError(() => {
          this.recipientName = 'Account not found';
          return of(null);
        })
      );
    }
    return of(null);
  })
).subscribe(account => {
  if (account) this.recipientName = `Recipient: ${account.holderName}`;
});
```

**`debounceTime(400)`** — Without this, an API call would fire on every single keystroke. With debounce, the API call only fires 400ms after the user stops typing. This is a standard UX pattern called "debouncing."

**`switchMap`** — If a new value arrives before the previous HTTP call completes, `switchMap` **cancels the in-flight request** and starts a new one. This prevents stale responses from overwriting newer ones.

**`ngOnDestroy()`** — Called when the component is destroyed (user navigates away):
```typescript
ngOnDestroy(): void {
  this.toAccountSub?.unsubscribe();   // Stop listening to form changes
  clearTimeout(this.toastTimeout);     // Cancel pending toast dismissal
  clearTimeout(this.redirectTimeout);  // Cancel pending redirect
}
```
Cleaning up subscriptions and timeouts prevents **memory leaks** — where the app uses more and more memory over time because old event listeners and timers are never removed.

**`submitTransfer()`:**
```typescript
submitTransfer(): void {
  if (this.transferForm.valid) {
    const request: TransferRequest = {
      fromAccountId: this.authService.getAccountId() ?? 0,
      toAccountId: this.transferForm.value.toAccountId,
      amount: this.transferForm.value.amount,
      idempotencyKey: this.generateIdempotencyKey(),  // crypto.randomUUID()
    };

    this.transferService.transfer(request).subscribe({
      next: (response: TransferResponse) => {
        if (response.status === 'SUCCESS') {
          this.accountService.refreshAccount(request.fromAccountId); // Update balance cache
          this.showSuccessAlert = true;
          // Auto-navigate to dashboard after 3 seconds
          this.redirectTimeout = setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 3000);
        }
      },
      error: (err: any) => {
        this.showErrorToast(err.message || 'Transfer failed. Please try again.');
      },
    });
  }
}
```

**`generateIdempotencyKey()`:**
```typescript
private generateIdempotencyKey(): string {
  return crypto.randomUUID();   // Browser's built-in UUID v4 generator
}
```
A new UUID is generated for every submit click. This ensures each transfer attempt has a unique key.

---

### `components/history-component/` — Transaction History Screen

**Route:** `/history`

**What is it?**
Displays all past transactions (sent and received) for the logged-in account. Includes filtering capabilities.

**`history-component.ts` explained:**

**Filter properties:**
```typescript
statusFilter: 'ALL' | 'SUCCESS' | 'FAILED' = 'ALL';
senderSearch = '';
recipientSearch = '';
dateSearch = '';
```

**`loadTransactions()`:**
```typescript
loadTransactions(): void {
  this.accountService.getAccountTransactions(this.accountId).subscribe({
    next: (data: TransactionLog[]) => {
      this.transactions = data;  // All transactions (unfiltered)
      this.isLoading = false;
      this.cdr.markForCheck();
    },
    error: ...
  });
}
```

**`get filteredTransactions()` — computed getter:**
```typescript
get filteredTransactions(): TransactionLog[] {
  return this.transactions.filter(t => {
    // Filter by status
    if (this.statusFilter !== 'ALL' && t.status !== this.statusFilter) return false;
    // Filter by recipient name/ID
    if (this.recipientSearch.trim()) { ... }
    // Filter by sender name/ID
    if (this.senderSearch.trim()) { ... }
    // Filter by date
    if (this.dateSearch) { ... }
    return true;
  });
}
```
A **computed getter** (marked with `get`) recalculates every time the template reads `filteredTransactions`. The template binds to `filteredTransactions`, so whenever a filter changes, the template automatically shows the correct subset.

**`get successfulCount()` and `get failedCount()`:**
```typescript
get successfulCount(): number { return this.transactions.filter(t => t.status === 'SUCCESS').length; }
get failedCount(): number { return this.transactions.filter(t => t.status === 'FAILED').length; }
```
Used to display summary statistics (e.g., "5 successful, 2 failed").

**`get hasFailedTransactions()`:**
```typescript
get hasFailedTransactions(): boolean {
  return this.filteredTransactions.some(t => t.status === 'FAILED' && t.failureReason);
}
```
Used to conditionally show a "Failed Transactions" section.

---

### `components/profile-component/` — Account Profile Screen

**Route:** `/profile`

**What is it?**
Displays the user's account details: account ID, holder name, balance, status, and last updated time. Read-only view.

**`profile-component.ts` explained:**

```typescript
account = signal<Account>({...}); // Reactive signal for account data

ngOnInit(): void {
  this.fetchAccount();
}

fetchAccount(): void {
  this.accountService.getAccount(this.authService.getAccountId() || 0).subscribe({
    next: (data) => { if (data) this.account.set(data); },
    error: (error) => { console.error('Failed to load account information', error); },
  });
}
```

**`getStatusBadgeClass()`:**
```typescript
getStatusBadgeClass(): string {
  switch (this.account()?.status) {
    case 'ACTIVE': return 'bg-success';  // Green badge
    case 'LOCKED': return 'bg-warning';  // Yellow badge
    case 'CLOSED': return 'bg-danger';   // Red badge
    default: return 'bg-secondary';
  }
}
```
Returns a CSS class name based on account status, used to color-code the status badge in the template.

---

### `components/reward-component/` — Reward Points Screen

**Route:** `/rewards`

**What is it?**
Displays the user's reward points history and total accumulated points. This is a **standalone component** (newer Angular pattern).

**What makes it standalone?**
```typescript
@Component({
  selector: 'app-reward',
  imports: [CommonModule, DatePipe],  // Imports modules directly
  ...
  changeDetection: ChangeDetectionStrategy.OnPush,
})
```
A standalone component imports its own dependencies (instead of relying on a module's `imports` array). It can be lazy-loaded without being declared in any `NgModule`.

**`inject()` function (modern Angular DI pattern):**
```typescript
private readonly rewardService = inject(RewardService);
private readonly authService = inject(AuthService);
private readonly router = inject(Router);
```
This is an alternative to constructor injection. `inject()` is called inside the class body (before the constructor). Both patterns work the same way — it's a newer, more concise syntax.

**Multiple signals:**
```typescript
readonly rewards = signal<Reward[]>([]);
readonly isLoading = signal(true);
readonly errorMessage = signal<string | null>(null);
readonly totalPoints = signal(0);
```

**Loading rewards:**
```typescript
private loadRewards(accountId: number): void {
  this.isLoading.set(true);
  this.rewardService.getRewards(accountId).subscribe({
    next: (rewards) => {
      this.rewards.set(rewards);
      // Calculate total points by summing all reward entries
      this.totalPoints.set(rewards.reduce((sum, reward) => sum + reward.pointsAwarded, 0));
      this.isLoading.set(false);
    },
    error: (error: Error) => {
      this.errorMessage.set(error.message || 'Unable to load rewards.');
      this.isLoading.set(false);
    },
  });
}
```

`Array.reduce()` is used to sum all `pointsAwarded` values into a single total. Start with `sum = 0`, add each `reward.pointsAwarded`, and return the final sum.

---

### `components/login-component/` — Admin Login (Legacy)

**Route:** `/admin-login`

**What is it?**
An older login form that directly takes username, password, and account ID without any backend validation. It uses `AuthService.login()` which just encodes the credentials and stores them.

```typescript
onSubmit(): void {
  if (this.loginForm.valid) {
    const { username, password, accountId } = this.loginForm.value;
    this.authService.login(username, password, accountId);
    this.router.navigate(['/dashboard']);
  }
}
```

**Why does it still exist?**
This was the original login method before the `/api/v1/users/login` endpoint was implemented. It's kept for backward compatibility or testing (admin can use this to directly log in with `admin:1234` and any account ID).

**Note:** The main entry for users is `StartPageComponent` (at `/start`), not this component.

---

### `components/top-navbar-component/` — Navigation Bar

**What is it?**
A reusable navigation bar shown at the top of protected pages. Contains navigation links and a logout button.

```typescript
export class TopNavbarComponent {
  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
```

Extremely simple — just delegates the logout action to `AuthService`. All the navigation links are in the `.html` template using `<a routerLink="/dashboard">Dashboard</a>` etc.

**`ChangeDetectionStrategy.OnPush`:** Performance optimization — the navbar rarely changes, so there's no need to check it on every update cycle.

---

## `app.spec.ts` / `*.spec.ts` — Test Files

**What are they?**
Files ending in `.spec.ts` are **unit test files**. Angular uses Jasmine (test framework) and Karma (test runner). Each `.spec.ts` file tests the corresponding TypeScript file.

For example, `account-service.spec.ts` tests `account-service.ts`.

**In this project**, the spec files are mostly scaffolding (empty or minimal). They exist because the Angular CLI generates them automatically. Writing proper tests would be the next step in maturing this codebase.

---

## Complete Navigation Flow

Here's how all the pieces work together when a user uses the app:

```
1. User visits http://localhost:4200
   → app.ts renders <router-outlet>
   → Angular router redirects '' to '/start'
   → StartPageComponent is rendered

2. User registers (Sign Up tab)
   → StartPageComponent.onSignup()
   → UserService.register() → POST /api/v1/users/register
   → AuthInterceptor: skips auth header (public endpoint)
   → UserController → UserServiceImpl.register()
   → Success: shows message, switches to Login tab

3. User logs in (Sign In tab)
   → StartPageComponent.onLogin()
   → UserService.login() → POST /api/v1/users/login
   → AuthInterceptor: skips auth header (public endpoint)
   → UserController → UserServiceImpl.login()
   → Success: AuthService.setUserSession(holderName, accountId)
     → stores admin token + accountId in localStorage
   → Router.navigate(['/dashboard'])

4. Dashboard loads
   → AuthGuard checks: isLoggedIn()? → true (token exists in localStorage)
   → DashboardComponent renders
   → ngOnInit() → AccountService.getAccount(accountId)
   → AuthInterceptor adds Authorization: Basic YWRtaW46MTIzNA== header
   → AccountController → AccountServiceImpl.getAccount()
   → MySQL returns Account data
   → account signal is set → UI re-renders with account details

5. User initiates a transfer
   → Navigates to /transfer
   → AuthGuard: passes
   → TransferComponent.ngOnInit(): loads balance
   → User types recipient ID → debounce + switchMap → live name lookup
   → User submits form
   → TransferComponent.submitTransfer()
   → TransferService.transfer() → POST /api/v1/transfers
   → AuthInterceptor adds auth header
   → TransferController → TransferServiceImpl.transfer()
     → validation → idempotency check → debit/credit → reward evaluation
   → Success: showSuccessAlert = true → redirect to /dashboard after 3s

6. User views history
   → Navigates to /history
   → HistoryComponent loads transactions
   → AccountService.getAccountTransactions(accountId)
   → GET /api/v1/accounts/{id}/transactions → returns all transfers
   → User applies filters → filteredTransactions getter re-calculates

7. User logs out
   → AuthService.logout()
   → localStorage cleared
   → Router.navigate(['/start'])
```

---

## Summary — Key Patterns Used

| Pattern | Where Used | Why |
|---|---|---|
| **Reactive Forms** | StartPageComponent, TransferComponent, LoginComponent | Built-in validation, easy to manage form state |
| **Signals** | DashboardComponent, ProfileComponent, RewardComponent | Modern Angular reactivity — automatic re-rendering |
| **BehaviorSubject** | AuthService | Reactive login state that components can subscribe to |
| **HTTP Interceptor** | AuthInterceptor | Centralized auth header injection for all requests |
| **Route Guards** | AuthGuard | Protect routes from unauthenticated access |
| **debounceTime + switchMap** | TransferComponent | Efficient live search — avoid excessive API calls |
| **Lazy Loading** | RewardComponent | Faster initial load by splitting code |
| **OnPush Change Detection** | Dashboard, Navbar, Reward | Performance optimization |
| **Standalone Components** | TransferComponent, RewardComponent | Newer Angular pattern — self-contained components |
| **inject()** | RewardComponent | Modern DI syntax (alternative to constructor injection) |
| **ngOnDestroy cleanup** | TransferComponent | Prevent memory leaks by unsubscribing and clearing timers |
