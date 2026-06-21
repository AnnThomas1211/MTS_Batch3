import { ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService, UserLoginResponse } from '../../service/user-service';
import { AuthService } from '../../service/auth-service';

const passwordMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const pw = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
};

@Component({
  selector: 'app-start-page-component',
  standalone: false,
  templateUrl: './start-page-component.html',
  styleUrl: './start-page-component.css',
})
export class StartPageComponent {
  activeTab: 'login' | 'signup' = 'login';

  loginForm: FormGroup;
  signupForm: FormGroup;

  isLoading = false;
  loginError: string | null = null;
  signupError: string | null = null;
  signupSuccess: string | null = null;
  isDuplicateEmail = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private userService: UserService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });

    this.signupForm = this.fb.group(
      {
        name: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordMatchValidator }
    );
  }

  setTab(tab: 'login' | 'signup'): void {
    this.activeTab = tab;
    this.loginError = null;
    this.signupError = null;
    this.signupSuccess = null;
    this.isDuplicateEmail = false;
    this.cdr.detectChanges();
  }

  onLogin(): void {
    if (this.loginForm.invalid) return;
    this.isLoading = true;
    this.loginError = null;

    const { email, password } = this.loginForm.value;

    this.userService.login({ email, password }).subscribe({
      next: (res: UserLoginResponse) => {
        this.isLoading = false;
        // Wire up the session using the admin Basic Auth token so all
        // existing API calls through the interceptor keep working
        this.authService.setUserSession(res.holderName, res.accountId);
        this.router.navigate(['/dashboard']);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        let errMsg = 'Invalid email or password.';
        if (err) {
          if (err.error) {
            if (typeof err.error === 'string') {
              errMsg = err.error;
            } else if (typeof err.error === 'object') {
              errMsg = err.error.message || err.error.error || JSON.stringify(err.error);
            }
          } else if (err.message) {
            errMsg = err.message;
          }
        }
        this.loginError = errMsg;
        this.cdr.detectChanges();
      },
    });
  }

  onSignup(): void {
    if (this.signupForm.invalid) return;
    this.isLoading = true;
    this.signupError = null;
    this.signupSuccess = null;
    this.isDuplicateEmail = false;

    const { name, email, password } = this.signupForm.value;

    this.userService.register({ name, email, password }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.signupSuccess = res.message + ' You can now sign in.';
        this.signupForm.reset();
        this.cdr.detectChanges();
        setTimeout(() => {
          this.setTab('login');
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err) => {
        this.isLoading = false;
        let errorMsg = 'Registration failed. Please try again.';
        if (err) {
          if (err.error) {
            if (typeof err.error === 'string') {
              errorMsg = err.error;
            } else if (typeof err.error === 'object') {
              errorMsg = err.error.message || err.error.error || JSON.stringify(err.error);
            }
          } else if (err.message) {
            errorMsg = err.message;
          }
        }
        if (err?.status === 409 || errorMsg.toLowerCase().includes('already exists')) {
          this.isDuplicateEmail = true;
          this.signupError = errorMsg;
        } else {
          this.signupError = errorMsg;
        }
        this.cdr.detectChanges();
      },
    });
  }
}
