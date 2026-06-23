import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly ACCOUNT_ID_KEY = 'account_id';
  private readonly USER_ROLE_KEY = 'user_role';
  private isBrowser: boolean;

  private loggedIn: BehaviorSubject<boolean>;
  isLoggedIn$;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object,
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.loggedIn = new BehaviorSubject<boolean>(this.hasToken());
    this.isLoggedIn$ = this.loggedIn.asObservable();
  }

  /**
   * Used for legacy Admin Login (from /admin-login, using the LoginComponent).
   * Encodes the provided admin username/password for Basic Auth.
   */
  login(username: string, password: string, accountId: number): void {
    const token = btoa(`${username}:${password}`);
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, username);
      localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
    }
    this.loggedIn.next(true);
  }

  /**
   * Called after a successful user login via /api/v1/users/login.
   * Stores the admin Basic Auth token so the interceptor can authenticate
   * all subsequent API calls (accounts, transfers, etc.) via Spring Security.
   */
  setUserSession(holderName: string, accountId: number): void {
    // Admin credentials are used for Basic Auth on all protected backend endpoints
    const token = btoa('admin:1234');
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, holderName);
      localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
    }
    this.loggedIn.next(true);
  }


  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      localStorage.removeItem(this.ACCOUNT_ID_KEY);
      localStorage.removeItem(this.USER_ROLE_KEY);
    }
    this.loggedIn.next(false);
    this.router.navigate(['/start']);
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem(this.TOKEN_KEY) : null;
  }

  getUsername(): string | null {
    return this.isBrowser ? localStorage.getItem(this.USER_KEY) : null;
  }

  getAccountId(): number | null {
    if (!this.isBrowser) return null;
    const id = localStorage.getItem(this.ACCOUNT_ID_KEY);
    return id ? Number(id) : null;
  }

  isLoggedIn(): boolean {
    return this.hasToken();
  }

  private hasToken(): boolean {
    return this.isBrowser ? !!localStorage.getItem(this.TOKEN_KEY) : false;
  }
}
