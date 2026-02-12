import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { AccountService } from '../../service/account-service';
import { TransactionLog } from '../../models/transaction-log';
import { ActivatedRoute } from '@angular/router';
import { timeStamp } from 'console';
import { forkJoin, Timestamp } from 'rxjs';
import { Account } from '../../models/account';

@Component({
  selector: 'app-history-component',
  standalone: false,
  templateUrl: './history-component.html',
  styleUrls: ['./history-component.css'], // also corrected
  changeDetection: ChangeDetectionStrategy.Default,
})
export class HistoryComponent implements OnInit{
  transactions: TransactionLog[] = [];
  // id = 2;
  isLoading = true;
  errorMessage = '';
  accountId = 2;
  constructor(
    private accountService: AccountService,
    private cdr: ChangeDetectorRef  // ← added
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.accountService.getAccountTransactions(this.accountId).subscribe({
      next: (data: TransactionLog[]) => {
        this.transactions = data;
        this.isLoading = false;
        this.cdr.markForCheck();   // ← added
      },
      error: (error) => {
        this.errorMessage = error.message;
        this.isLoading = false;
        this.cdr.markForCheck();   // ← added
      }
    });
  }

  get failedTransactions(): TransactionLog[] {
    return this.transactions.filter(t => t.status === 'FAILED' && t.failureReason);
  }

  get successfulCount(): number {
    return this.transactions.filter(t => t.status === 'SUCCESS').length;
  }

  get failedCount(): number {
    return this.transactions.filter(t => t.status === 'FAILED').length;
  }

  get hasFailedTransactions(): boolean {
    return this.transactions.some(t => t.status === 'FAILED' && t.failureReason);
  }
}
