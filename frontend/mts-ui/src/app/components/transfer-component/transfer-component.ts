// transfer.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TransferService } from '../../service/transfer-service';
import { TransferRequest } from '../../models/transfer-request';
import { TransferResponse } from '../../models/transfer-response';
import { Router } from '@angular/router';
import { AccountService } from '../../service/account-service';

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

  constructor(
    private fb: FormBuilder,
    private transferService: TransferService,
    private router: Router,
    private accountService: AccountService,
  ) {
    this.transferForm = this.fb.group({
      fromAccountId: [''],
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
          this.resultMessage = response.message;
          // Invalidate account cache after successful transfer
          if (this.success) {
            this.accountService.refreshAccount(request.fromAccountId);
            this.router.navigate(['/']);
          }
        },
        error: () => {
          this.success = false;
          this.resultMessage = 'Transfer failed. Please try again.';
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
}
