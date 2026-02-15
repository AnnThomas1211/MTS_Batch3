export interface TransferResponse {
  transactionId: string
  status: string
  message: string
  debitedFrom: number
  creditedTo: number
  amount: number
}
