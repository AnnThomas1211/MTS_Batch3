import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TransferService } from '../../service/transfer-service';
import { TransferRequest } from '../../models/transfer-request';
import { TransferResponse } from '../../models/transfer-response';
import { Router } from '@angular/router';
import { AccountService } from '../../service/account-service';
import { AuthService } from '../../service/auth-service';
import { Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transfer-component.html',
  styleUrls: ['./transfer-component.css'],
})
export class TransferComponent implements OnInit, OnDestroy {
  transferForm: FormGroup;
  recipientName: string | null = null;
  availableBalance: number | null = null;
  resultMessage: string | null = null;
  success: boolean | null = null;
  toastVisible = false;
  toastMessage: string | null = null;

  private toastTimeout: any;
  private toAccountSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private transferService: TransferService,
    private router: Router,
    private accountService: AccountService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    const accountId = this.authService.getAccountId();
    const username = this.authService.getUsername();
    const displayDetails = username ? `${username} (Account #${accountId})` : `${accountId}`;

    this.transferForm = this.fb.group({
      fromAccountId: [{value: displayDetails, disabled: true}],
      toAccountId: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
    });
  }

  ngOnInit(): void {
    const accountId = this.authService.getAccountId() ?? 0;
    this.accountService.getAccount(accountId).subscribe({
      next: (acc) => {
        if (acc) {
          this.availableBalance = acc.balance;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Failed to load account balance', err);
      }
    });

    this.toAccountSub = this.transferForm.get('toAccountId')?.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(val => {
        const id = Number(val);
        if (id && !isNaN(id) && id > 0) {
          if (id === this.authService.getAccountId()) {
            this.recipientName = 'Cannot transfer to yourself';
            return of(null);
          }
          return this.accountService.getAccount(id).pipe(
            catchError(() => {
              this.recipientName = 'Account not found';
              this.cdr.detectChanges();
              return of(null);
            })
          );
        } else {
          this.recipientName = null;
          return of(null);
        }
      })
    ).subscribe(account => {
      if (account) {
        this.recipientName = `Recipient: ${account.holderName}`;
      } else if (this.recipientName !== 'Cannot transfer to yourself' && this.recipientName !== 'Account not found') {
        this.recipientName = null;
      }
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.toAccountSub?.unsubscribe();
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
  }

  private generateIdempotencyKey(): string {
    return crypto.randomUUID();
  }

  submitTransfer(): void {
    if (this.transferForm.valid) {
      const request: TransferRequest = {
        fromAccountId: this.authService.getAccountId() ?? 0,
        toAccountId: this.transferForm.value.toAccountId,
        amount: this.transferForm.value.amount,
        idempotencyKey: this.generateIdempotencyKey(),
      };

      this.transferService.transfer(request).subscribe({
        next: (response: TransferResponse) => {
          this.success = response.status === 'SUCCESS';
          if (this.success) {
            this.resultMessage = response.message;
            this.accountService.refreshAccount(request.fromAccountId);
            this.router.navigate(['/dashboard']);
          } else {
            this.resultMessage = null;
          }
        },

        error: (err: any) => {
          this.resultMessage = null;
          this.success = null;
          this.showErrorToast(err.message || 'Transfer failed. Please try again.');
        },
      });
    }
  }

  cancel(): void {
    this.transferForm.reset();
    this.recipientName = null;
    this.resultMessage = null;
    this.success = null;
    this.router.navigate(['/dashboard']);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goToTransfer(): void {
    this.router.navigate(['/transfer']);
  }

  goToHistory(): void {
    this.router.navigate(['/history']);
  }

  showErrorToast(message: string): void {
    clearTimeout(this.toastTimeout);
    this.toastVisible = false;
    this.toastMessage = null;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.toastMessage = message;
      this.toastVisible = true;
      this.cdr.detectChanges();
    }, 0);

    this.toastTimeout = setTimeout(() => {
      this.toastVisible = false;
      this.toastMessage = null;
      this.cdr.detectChanges();
    }, 5000);
  }

  dismissToast(): void {
    this.toastVisible = false;
    this.toastMessage = null;
    clearTimeout(this.toastTimeout);
    this.cdr.detectChanges();
  }
}
