// transfer.service.ts
import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { Observable, throwError } from 'rxjs'
import { catchError } from 'rxjs/operators'
import { TransferRequest } from '../models/transfer-request'
import { TransferResponse } from '../models/transfer-response'

@Injectable({
  providedIn: 'root'
})
export class TransferService {

  private apiUrl = 'http://localhost:8080/api/v1/transfers'

  constructor(private http: HttpClient) {}

  transfer(request: TransferRequest): Observable<TransferResponse> {
    return this.http.post<TransferResponse>(this.apiUrl, request).pipe(
      catchError(this.handleError)
    )
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred'
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`
    } else {
      // Server-side error
      if (error.status === 403) {
        errorMessage = 'Access denied: You are not authorized to perform this transfer.'
      } else if (error.status === 400) {
        errorMessage = error.error?.message || 'Invalid request: Please check your input.'
      } else if (error.status === 404) {
        errorMessage = 'Account not found.'
      } else {
        errorMessage = error.error?.message || `Server error: ${error.status}`
      }
    }

    return throwError(() => new Error(errorMessage))
  }
}
