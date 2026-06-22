import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Account } from '../models/account';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly URL = 'http://localhost:8080/api/v1/accounts';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly ACCOUNT_ID_KEY = 'account_id';
  private isBrowser: boolean;

  private loggedIn: BehaviorSubject<boolean>;
  isLoggedIn$;

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object,
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.loggedIn = new BehaviorSubject<boolean>(this.hasToken());
    this.isLoggedIn$ = this.loggedIn.asObservable();
  }

  /**
   * Validates the credentials against the backend. The session is only
   * established (token + account stored, logged-in state emitted) if the
   * backend confirms the account id + password. Callers should navigate only
   * after this observable emits successfully.
   */
  login(accountId: number, password: string): Observable<Account> {
    return this.http
      .post<Account>(`${this.URL}/login`, { accountId, password })
      .pipe(
        tap((account) => this.establishSession(account, accountId, password))
      );
  }

  /**
   * Registers a new account from a holder name + password. The backend assigns
   * the account id, which is returned in the response so the user knows the id
   * they will sign in with. No session is established here.
   */
  register(holderName: string, password: string): Observable<Account> {
    return this.http.post<Account>(`${this.URL}/register`, { holderName, password });
  }

  private establishSession(account: Account, accountId: number, password: string): void {
    // HTTP Basic credentials for subsequent requests are accountId:password,
    // matched against the per-account BCrypt hash on the backend.
    const token = btoa(`${accountId}:${password}`);
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, account.holderName ?? '');
      localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
    }
    this.loggedIn.next(true);
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      localStorage.removeItem(this.ACCOUNT_ID_KEY);
    }
    this.loggedIn.next(false);
    this.router.navigate(['/login']);
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
