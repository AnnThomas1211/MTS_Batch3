import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-register-component',
  standalone: false,
  templateUrl: './register-component.html',
  styleUrl: './register-component.css',
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.registerForm = this.fb.group({
      holderName: ['', [Validators.required, Validators.maxLength(100)]],
      password: ['', [Validators.required, Validators.minLength(4)]],
    });
  }

  onSubmit(): void {
    this.errorMessage = null;

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const { holderName, password } = this.registerForm.value;
    this.isSubmitting = true;

    this.authService.register(holderName.trim(), password).subscribe({
      next: (account) => {
        this.isSubmitting = false;
        // Redirect to the login page and pass the new account id along so the
        // login page can show a popup telling the user their id.
        this.router.navigate(['/login'], {
          queryParams: { registered: account.id },
        });
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.errorMessage = this.resolveErrorMessage(err);
        // Zoneless app: trigger change detection so the error shows immediately.
        this.cdr.markForCheck();
      },
    });
  }

  private resolveErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Unable to reach the server. Please check your connection and try again.';
    }

    const backendMessage = err.error?.message as string | undefined;

    switch (err.status) {
      case 400:
        return backendMessage ?? 'Please enter a valid name and password.';
      default:
        return backendMessage ?? 'Something went wrong while creating your account. Please try again.';
    }
  }
}
