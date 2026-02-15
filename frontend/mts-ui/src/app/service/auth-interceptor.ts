import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth-service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler): Observable<HttpEvent<any>> {
    const authToken = this.auth.getToken();
    if (authToken) {
      const clonedReq = req.clone({
        setHeaders: {
          Authorization: `Basic ${authToken}`,
        }
      });
      return next.handle(clonedReq);
    }
    return next.handle(req);
  }
}
