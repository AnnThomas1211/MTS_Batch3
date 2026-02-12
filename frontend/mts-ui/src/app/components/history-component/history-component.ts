import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { AccountService } from '../../service/account-service';
import { TransactionLog } from '../../models/transaction-log';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-history-component',
  standalone: false,
  templateUrl: './history-component.html',
  styleUrl: './history-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoryComponent {
//   transactionDisplay: TransactionDisplay[] = [];
//   id = 0;

//   constructor(
//     private service: AccountService,
//     private activatedRoute: ActivatedRoute,
//   ) {
//     this.activatedRoute.params.subscribe((params) => {
//       this.id = params['id'];
//     });
//   }

//   ngOnInit(): void {
//     this.service.getAccountTransactions(this.id).pipe(
//       switchMap((transactions: TransactionLog[]) => {
//         // If no transactions, return empty array
//         if (!transactions || transactions.length === 0) {
//           return [];
//         }

//         // Create an array of observables to fetch account names
//         const accountRequests = transactions.map((transaction) => {
//           const otherAccountId = transaction.fromAccountId == this.id
//             ? transaction.toAccountId
//             : transaction.fromAccountId;

//           return this.service.getAccount(otherAccountId).pipe(
//             map((account) => ({
//               amount: transaction.amount,
//               isSender: transaction.fromAccountId == this.id,
//               status: transaction.status,
//               name: account.holderName,
//               time: transaction.createdOn,
//             }))
//           );
//         });

//         // Wait for all account requests to complete
//         return forkJoin(accountRequests);
//       })
//     ).subscribe((displayData: TransactionDisplay[]) => {
//       this.transactionDisplay = displayData;
//     });
//   }

//   getStatusClass(status: string): string {
//     switch (status?.toLowerCase()) {
//       case 'success':
//       case 'completed':
//         return 'text-success';
//       case 'pending':
//         return 'text-warning';
//       case 'failed':
//         return 'text-danger';
//       default:
//         return 'text-secondary';
//     }
//   }

//   formatAmount(amount: number, isSender: boolean): string {
//     const prefix = isSender ? '-' : '+';
//     return `${prefix}₹${amount.toFixed(2)}`;
//   }

//   getAmountClass(isSender: boolean): string {
//     return isSender ? 'text-danger' : 'text-success';
//   }
// }

// export interface TransactionDisplay {
//   isSender: boolean;
//   name: string;
//   amount: number;
//   status: string;
//   time: Date;
}
