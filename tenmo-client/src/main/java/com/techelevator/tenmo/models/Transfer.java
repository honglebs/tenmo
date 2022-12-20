package com.techelevator.tenmo.models;

import java.math.BigDecimal;

// This class represents a trimmed-down version of a Transfer object,
// composed only of userIdTo and amount
public class Transfer {
//    private int transferId;
//    private int transferTypeId;
//    private int transferStatusId;
//    private int accountFrom;
//    private int accountTo;

    // Since client should not know the account number of the user they're sending money to,
    // use userIdTo instead. We will not include the userId of the sender because this
    // will be determined on the server side
    private int userIdTo;
    private BigDecimal amount;

    public Transfer() {
    }

//    public Transfer(int transferId, int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount) {
////        this.transferId = transferId;
//        this.transferTypeId = transferTypeId;
//        this.transferStatusId = transferStatusId;
////        this.accountFrom = accountFrom;
////        this.accountTo = accountTo;
//        this.amount = amount;
//    }

//    public int getTransferId() {
//        return transferId;
//    }
//
//    public void setTransferId(int transferId) {
//        this.transferId = transferId;
//    }




//    public int getTransferTypeId() {
//        return transferTypeId;
//    }
//
//    public void setTransferTypeId(int transferTypeId) {
//        this.transferTypeId = transferTypeId;
//    }
//
//    public int getTransferStatusId() {
//        return transferStatusId;
//    }
//
//    public void setTransferStatusId(int transferStatusId) {
//        this.transferStatusId = transferStatusId;
//    }

//    public int getAccountFrom() {
//        return accountFrom;
//    }
//
//    public void setAccountFrom(int accountFrom) {
//        this.accountFrom = accountFrom;
//    }
//
//    public int getAccountTo() {
//        return accountTo;
//    }
//
//    public void setAccountTo(int accountTo) {
//        this.accountTo = accountTo;
//    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getUserIdTo() {
        return userIdTo;
    }

    public void setUserIdTo(int userIdTo) {
        this.userIdTo = userIdTo;
    }
}
