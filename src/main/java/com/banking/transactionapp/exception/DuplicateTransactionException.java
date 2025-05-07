package com.banking.transactionapp.exception;

public class DuplicateTransactionException extends RuntimeException {
    
    public DuplicateTransactionException() {
        super("A duplicate transaction already exists");
    }
    
    public DuplicateTransactionException(long timeWindowSeconds) {
        super("A duplicate transaction was detected within " + timeWindowSeconds + " seconds");
    }
}
