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

    const isPublicAuthRequest =
      req.url.endsWith('/accounts/login') || req.url.endsWith('/accounts/register');

    let authReq = req;
    if (token && !isPublicAuthRequest) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Basic ${token}`,
        },
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !isPublicAuthRequest) {
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
