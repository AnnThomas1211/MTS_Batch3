import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { AccountService } from '../../service/account-service';
import { TransactionLog } from '../../models/transaction-log';


@Component({
  selector: 'app-history-component',
  standalone: false,
  templateUrl: './history-component.html',
  styleUrl: './history-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoryComponent implements OnInit {
  transactions: TransactionLog[] = [];
  isLoading = true;
  errorMessage: string = '';

  accountId: number = 1;

  constructor(private accountService: AccountService){}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.accountService.getAccountTransactions(this.accountId).subscribe({
      next: (data : TransactionLog[]) => {
        this.transactions = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.message;
        this.isLoading = false;
      }
    })
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
