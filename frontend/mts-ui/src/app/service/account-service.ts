import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, shareReplay } from 'rxjs/operators';
import { Account } from '../models/account';
import { TransactionLog } from '../models/transaction-log';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private URL = "http://localhost:8080/api/v1/accounts"
  private accountCache = new Map<number, Observable<Account>>();
  private accountDataCache = new Map<number, Account>();

  constructor(private http: HttpClient) { }

  getAccount(id: number) {
    return this.http.get<Account>(`${this.URL}/${id}`)
  }

  getAccountBalance(id: number) {
    return this.http.get<number>(`${this.URL}/${id}/balance`)
  }

  getAccountTransactions(id: number) {
    console.log(id);
    let resp = this.http.get<TransactionLog[]>(`${this.URL}/${id}/transactions`)
    return resp;
  }

  // Method to manually refresh account data
  refreshAccount(id: number) {
    this.accountCache.delete(id);
    this.accountDataCache.delete(id);
    return this.getAccount(id);
  }
}

