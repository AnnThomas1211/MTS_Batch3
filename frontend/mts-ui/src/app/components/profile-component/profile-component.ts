import { Component, computed, effect, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountService } from '../../service/account-service';
import { Account } from '../../models/account';
import { AccountStatus } from '../../enums/AccountStatus';

@Component({
  selector: 'app-profile-component',
  standalone: false,
  templateUrl: './profile-component.html',
  styleUrl: './profile-component.css',
})
export class ProfileComponent implements OnInit{
  appVersion = '0.0.0';
  account = signal<Account>({
    id: 0,
    holderName: '',
    balance: 0,
    status: AccountStatus.ACTIVE,
    lastUpdated: new Date(),
  });

  constructor(private accountService: AccountService) {
  }

  ngOnInit(): void {
      this.fetchAccount();
  }

  fetchAccount(): void {
    this.accountService.getAccount(2).subscribe({
      next: (data) => {
        if (data) {
          this.account.set(data);
          return;
        }

      },
      error: (error) => {
        console.error('Failed to load account information', error);

      },
    });
  }

  getStatusBadgeClass(): string {
    switch (this.account()?.status) {
      case 'ACTIVE':
        return 'bg-success';
      case 'LOCKED':
        return 'bg-warning';
      case 'CLOSED':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }
}