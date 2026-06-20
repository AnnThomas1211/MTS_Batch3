import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Reward } from '../models/reward';

@Injectable({
  providedIn: 'root',
})
export class RewardService {
  private readonly url = 'http://localhost:8080/api/v1/accounts/rewards';

  constructor(private http: HttpClient) {}

  getRewards(accountId: number): Observable<Reward[]> {
    return this.http.get<Reward[]>(`${this.url}/${accountId}`).pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    const errorMessage =
      error.error instanceof ErrorEvent
        ? `Error: ${error.error.message}`
        : error.error?.message || `Server error: ${error.status}`;

    return throwError(() => new Error(errorMessage));
  }
}
