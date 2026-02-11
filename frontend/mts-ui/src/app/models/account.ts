export interface Account {
    id : number,
    holderName : string,
    balance : number,
    status : AccountStatus,
    lastUpdated : Date
}
