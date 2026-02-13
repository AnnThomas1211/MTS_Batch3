import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { Account } from '../../models/account';
import { AccountService } from '../../service/account-service';
import { AccountStatus } from '../../enums/AccountStatus';

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

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount() {
    // temp substitute accId
    this.accountService.getAccount(2).subscribe((data) => {
      if (data)
        this.account.set(data);
    });
    console.log(this.account().id);
  }
}
