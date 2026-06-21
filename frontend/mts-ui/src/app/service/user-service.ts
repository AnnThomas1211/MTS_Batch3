import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly BASE_URL = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  register(request: UserRegistrationRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.BASE_URL}/register`, request);
  }

  login(request: UserLoginRequest): Observable<UserLoginResponse> {
    return this.http.post<UserLoginResponse>(`${this.BASE_URL}/login`, request);
  }
}
