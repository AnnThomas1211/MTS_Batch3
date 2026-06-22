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
  errorToast: string | null = null;

  // ADDED: State to control modal visibility
  showModal: boolean = false;

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

  // ADDED: Intercepts the form submission to show the modal first
  openConfirmation(): void {
    if (this.transferForm.valid) {
      this.showModal = true;
    }
  }

  // ADDED: Closes the modal safely if they opt out
  closeConfirmation(): void {
    this.showModal = false;
  }

  // ADDED: Triggered when user clicks "Confirm & Send" inside the popup modal
  confirmAndSubmit(): void {
    this.closeConfirmation();
    this.submitTransfer();
  }

  // MODIFIED: Kept private to this class context or called by confirmAndSubmit
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
          this.resultMessage = response.message;
          // Zoneless app: notify change detection so the result renders now.
          this.cdr.markForCheck();
          if (this.success) {
            this.accountService.refreshAccount(request.fromAccountId);
            this.router.navigate(['/']);
          }
        },

        error: (err: any) => {
          // Prefer the backend's specific message (e.g. insufficient balance,
          // account not active) over Angular's generic HTTP error text.
          const message =
            err?.error?.message || err?.message || 'Transfer failed. Please try again.';
          this.showErrorToast(message);
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
    this.errorToast = message;
    // Render the toast immediately (may be called from an async HTTP callback).
    this.cdr.markForCheck();
    clearTimeout(this.toastTimeout);
    this.toastTimeout = setTimeout(() => {
      this.errorToast = null;
      // Auto-dismiss also runs outside any event, so notify CD again.
      this.cdr.markForCheck();
    }, 5000);
  }

  dismissToast(): void {
    this.errorToast = null;
    clearTimeout(this.toastTimeout);
  }
}
