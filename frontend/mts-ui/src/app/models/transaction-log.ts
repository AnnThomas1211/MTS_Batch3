export interface TransactionLog {
    id : string,
    fromAccountId : number,
    toAccountId : number,
    amount : number,
    status : TransactionStatus,
    failureReason? : string,
    idempotencyKey : string,
    createdOn : Date
}


