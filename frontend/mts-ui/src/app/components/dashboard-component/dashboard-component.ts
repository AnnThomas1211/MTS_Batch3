import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { Account } from '../../models/account';
import { AccountService } from '../../service/account-service';
import { AccountStatus } from '../../enums/AccountStatus';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-dashboard-component',
  standalone: false,
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {

  public account = signal<Account>({
    id: 0,
    holderName: '',
    balance: 0,
    status: AccountStatus.ACTIVE,
    lastUpdated: new Date()
  });

  constructor(
    private accountService: AccountService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount() {
    const accId = this.authService.getAccountId() ?? 0;
    this.accountService.getAccount(accId).subscribe((data) => {
      if (data)
        this.account.set(data);
    });
    console.log(this.account().id);
  }

  logout() {
    this.authService.logout();
  }
}
