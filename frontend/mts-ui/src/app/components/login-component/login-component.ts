import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-login-component',
  standalone: false,
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  isSubmitting = false;

  // Set when the user has just registered; drives the "account created" popup.
  registeredAccountId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    private cdr: ChangeDetectorRef
  ) {
    this.loginForm = this.fb.group({
      accountId: ['', [Validators.required, Validators.min(1)]],
      password: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    // After a successful registration we arrive here with ?registered=<id>.
    // Show a popup with the id and pre-fill it so signing in is one step.
    const registered = this.route.snapshot.queryParamMap.get('registered');
    if (registered) {
      this.registeredAccountId = Number(registered);
      this.loginForm.patchValue({ accountId: this.registeredAccountId });
    }
  }

  dismissPopup(): void {
    this.registeredAccountId = null;
    // Strip the ?registered query param from the URL WITHOUT a router
    // navigation, so the login form (incl. the pre-filled account id) is
    // preserved and the popup doesn't reappear on refresh.
    this.location.replaceState('/login');
  }

  onSubmit(): void {
    this.errorMessage = null;

    if (this.loginForm.invalid) {
      // Reveal field-level messages if the user submits an incomplete form.
      this.loginForm.markAllAsTouched();
      return;
    }

    const { accountId, password } = this.loginForm.value;
    this.isSubmitting = true;

    this.authService.login(Number(accountId), password).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.errorMessage = this.resolveErrorMessage(err);
        // Zoneless app: the error arrives in an async callback, so explicitly
        // trigger change detection or the message won't show until the next click.
        this.cdr.markForCheck();
      },
    });
  }

  private resolveErrorMessage(err: HttpErrorResponse): string {
    // status 0 -> request never reached the server (backend down / CORS / network).
    if (err.status === 0) {
      return 'Unable to reach the server. Please check your connection and try again.';
    }

    // Prefer the backend's specific message when present.
    const backendMessage = err.error?.message as string | undefined;

    switch (err.status) {
      case 400:
        return backendMessage ?? 'Please enter a valid Account ID and password.';
      case 401:
        return backendMessage ?? 'Incorrect password. Please try again.';
      case 404:
        return backendMessage ?? 'No account found with that Account ID.';
      default:
        return backendMessage ?? 'Something went wrong while signing in. Please try again.';
    }
  }
}
