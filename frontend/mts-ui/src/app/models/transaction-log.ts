export interface TransactionLog {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  fromAccountName: string;
  toAccountName: string;
  amount: number;
  status: TransactionStatus;
  failureReason?: string;
  idempotencyKey: string;
  createdOn: Date;
}
