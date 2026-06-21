import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../service/auth-service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // Public user endpoints (register / login) must NOT carry an auth header.
    // If a stale token exists in localStorage, Spring Security would reject the
    // request with 401 before it ever reaches the controller.
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
        // Only force logout when a PROTECTED endpoint returns 401.
        if (error.status === 401 && !isPublicUserEndpoint) {
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}

