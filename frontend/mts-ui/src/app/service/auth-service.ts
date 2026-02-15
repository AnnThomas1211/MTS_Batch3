import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, map, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private baseUrl = 'http://localhost:8080/api/v1/accounts/2';

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<any> {

    const token = btoa(`${username}:${password}`);

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Basic ${token}`,
    });

    return new Observable(observer => {
      this.http.get(`${this.baseUrl}`, { headers }).subscribe({
        next: (res) =>{
          if (this.storageAvailable()) {
            sessionStorage.setItem('authToken', token);
            sessionStorage.setItem('username', username);
          }
          observer.next(res);
          observer.complete();
        },
        error: (err) => {
          observer.error(err);
        }
      });
    });
  }

  getToken(): string | null {
    if (!this.storageAvailable()) return null;
    return sessionStorage.getItem('authToken');
  }

  logout(): void {
    if (this.storageAvailable()) {
      sessionStorage.clear();
    }
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    if (!this.storageAvailable()) return false;
    return !! sessionStorage.getItem('authToken');
  }

  getUsername(): string | null {
    if (!this.storageAvailable()) return '';
    return sessionStorage.getItem('username') || '';
  }

  private storageAvailable(): boolean {
    try {
      return typeof window !== 'undefined' && !!window.sessionStorage;
    } catch (e) {
      return false;
    }
  }

}

