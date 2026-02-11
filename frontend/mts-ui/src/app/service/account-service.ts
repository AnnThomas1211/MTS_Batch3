import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Account } from '../models/account';
import { TransactionLog } from '../models/transaction-log';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private URL = "http://localhost:8080/api/v1/accounts"
  constructor(private http: HttpClient) {}

  getAccount(id: number) {
    return this.http.get<Account>(`${URL}/${id}`)
  }

  getAccountBalance(id: number) {
    return this.http.get<number>(`${URL}/${id}/balance`)
  }

  getAccountTransactions(id: number) {
    console.log(id);
    let resp = this.http.get<TransactionLog[]>(`http://localhost:8080/api/v1/accounts/${id}/transactions`)
    return resp;
  }


  
}

