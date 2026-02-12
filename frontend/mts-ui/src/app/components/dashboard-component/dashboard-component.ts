import { Component, OnInit } from '@angular/core';
import { Account } from '../../models/account';
import { AccountService } from '../../service/account-service';

@Component({
  selector: 'app-dashboard-component',
  standalone: false,
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
})
export class DashboardComponent implements OnInit{

  account? : Account;

  constructor(private accountService : AccountService){}

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount() {
    // temp substitute accId
    this.accountService.getAccount(2)
    .subscribe(data => this.account = data);
    console.log(this.account?.id);
  }
}
