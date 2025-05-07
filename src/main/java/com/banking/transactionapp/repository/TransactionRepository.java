package com.banking.transactionapp.repository;

import com.banking.transactionapp.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    
    Transaction save(Transaction transaction);
    
    Optional<Transaction> findById(UUID id);
    
    List<Transaction> findAll();
    
    List<Transaction> findAll(int page, int size);
    
    long count();
    
    void deleteById(UUID id);
    
    boolean existsById(UUID id);
    
    boolean isDuplicate(Transaction transaction);
    
    /**
     * Checks if a transaction is a duplicate within a specified time window.
     * 
     * @param transaction The transaction to check
     * @param timeWindowSeconds The time window in seconds (0 or negative for exact match)
     * @return true if a duplicate exists, false otherwise
     */
    boolean isDuplicateWithinTimeWindow(Transaction transaction, long timeWindowSeconds);
}
