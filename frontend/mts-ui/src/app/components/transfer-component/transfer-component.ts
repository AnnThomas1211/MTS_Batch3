import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TransferService } from '../../service/transfer-service';
import { TransferRequest } from '../../models/transfer-request';
import { TransferResponse } from '../../models/transfer-response';
import { Router } from '@angular/router';
import { AccountService } from '../../service/account-service';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transfer-component.html',
  styleUrls: ['./transfer-component.css'],
})
export class TransferComponent {
  transferForm: FormGroup;
  resultMessage: string | null = null;
  success: boolean | null = null;
  toastVisible = false;
  toastMessage: string | null = null;

  private toastTimeout: any;

  constructor(
    private fb: FormBuilder,
    private transferService: TransferService,
    private router: Router,
    private accountService: AccountService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    const accountId = this.authService.getAccountId();
    this.transferForm = this.fb.group({
      fromAccountId: [{value: accountId, disabled: true}],
      toAccountId: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
    });
  }

  private generateIdempotencyKey(): string {
    return crypto.randomUUID();
  }

  submitTransfer(): void {
    if (this.transferForm.valid) {
      const request: TransferRequest = {
        fromAccountId: this.transferForm.getRawValue().fromAccountId,
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
            this.router.navigate(['/']);
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
    this.resultMessage = null;
    this.success = null;
    this.router.navigate(['/']);
  }

  goToDashboard(): void {
    this.router.navigate(['/']);
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
