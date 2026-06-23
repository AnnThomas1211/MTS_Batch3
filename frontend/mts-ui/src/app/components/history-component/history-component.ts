import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AccountService } from '../../service/account-service';
import { TransactionLog } from '../../models/transaction-log';
import { AuthService } from '../../service/auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-history-component',
  standalone: false,
  templateUrl: './history-component.html',
  styleUrls: ['./history-component.css'], // also corrected
  changeDetection: ChangeDetectionStrategy.Default,
})
export class HistoryComponent implements OnInit {
  transactions: TransactionLog[] = [];
  isLoading = true;
  errorMessage = '';
  accountId = 0;

  // Filter properties
  statusFilter: 'ALL' | 'SUCCESS' | 'FAILED' = 'ALL';
  senderSearch = '';
  recipientSearch = '';
  dateSearch = '';

  constructor(
    private accountService: AccountService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  ngOnInit(): void {
    this.accountId = this.authService.getAccountId() ?? 0;
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

  // whenever a filter is added, this automatically recalculates
  get filteredTransactions(): TransactionLog[] {
    return this.transactions.filter(t => {
      // 1. Status Filter
      if (this.statusFilter !== 'ALL' && t.status !== this.statusFilter) {
        return false;
      }

      // 2. Recipient Search
      if (this.recipientSearch.trim()) {
        const query = this.recipientSearch.toLowerCase().trim();
        const toName = t.toAccountName ? t.toAccountName.toLowerCase() : '';
        const toId = t.toAccountId ? String(t.toAccountId) : '';
        if (!toName.includes(query) && !toId.includes(query)) {
          return false;
        }
      }

      // 3. Sender Search
      if (this.senderSearch.trim()) {
        const query = this.senderSearch.toLowerCase().trim();
        const fromName = t.fromAccountName ? t.fromAccountName.toLowerCase() : '';
        const fromId = t.fromAccountId ? String(t.fromAccountId) : '';
        if (!fromName.includes(query) && !fromId.includes(query)) {
          return false;
        }
      }

      // 4. Date Search
      if (this.dateSearch) {
        const parts = this.dateSearch.split('-');
        if (parts.length === 3) {
          const y = Number(parts[0]);
          const m = Number(parts[1]);
          const d = Number(parts[2]);
          const txDate = new Date(t.createdOn);
          if (
            txDate.getFullYear() !== y ||
            (txDate.getMonth() + 1) !== m ||
            txDate.getDate() !== d
          ) {
            return false;
          }
        }
      }

      return true;
    });
  }

  onStatusFilterChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.statusFilter = select.value as 'ALL' | 'SUCCESS' | 'FAILED';
    this.cdr.markForCheck();
  }

  onSenderSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.senderSearch = input.value;
    this.cdr.markForCheck();
  }

  onRecipientSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.recipientSearch = input.value;
    this.cdr.markForCheck();
  }

  onDateSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateSearch = input.value;
    this.cdr.markForCheck();
  }

  clearFilters(): void {
    this.statusFilter = 'ALL';
    this.senderSearch = '';
    this.recipientSearch = '';
    this.dateSearch = '';
    this.cdr.markForCheck();
  }

  get failedTransactions(): TransactionLog[] {
    return this.filteredTransactions.filter(t => t.status === 'FAILED' && t.failureReason);
  }

  get successfulCount(): number {
    return this.transactions.filter(t => t.status === 'SUCCESS').length;
  }

  get failedCount(): number {
    return this.transactions.filter(t => t.status === 'FAILED').length;
  }

  get hasFailedTransactions(): boolean {
    return this.filteredTransactions.some(t => t.status === 'FAILED' && t.failureReason);
  }
}
