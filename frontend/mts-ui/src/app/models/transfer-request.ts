export interface TransferRequest {
  fromAccountId: number
  toAccountId: number
  amount: number
  idempotencyKey: string
}
