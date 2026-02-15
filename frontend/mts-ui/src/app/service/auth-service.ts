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

  login(username: string, password: string, accountId: number): void {
    const token = btoa(`${username}:${password}`);
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.USER_KEY, username);
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
