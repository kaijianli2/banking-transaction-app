package com.banking.transactionapp.repository;

import com.banking.transactionapp.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    
    private final Map<UUID, Transaction> transactionStore = new ConcurrentHashMap<>();
    
    @Override
    public Transaction save(Transaction transaction) {
        transactionStore.put(transaction.getId(), transaction);
        return transaction;
    }
    
    @Override
    public Optional<Transaction> findById(UUID id) {
        return Optional.ofNullable(transactionStore.get(id));
    }
    
    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactionStore.values());
    }
    
    @Override
    public List<Transaction> findAll(int page, int size) {
        return transactionStore.values().stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return transactionStore.size();
    }
    
    @Override
    public void deleteById(UUID id) {
        transactionStore.remove(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return transactionStore.containsKey(id);
    }
    
    @Override
    public boolean isDuplicate(Transaction transaction) {
        // Default implementation for backward compatibility
        return isDuplicateWithinTimeWindow(transaction, 0);
    }
    
    @Override
    public boolean isDuplicateWithinTimeWindow(Transaction transaction, long timeWindowSeconds) {
        return transactionStore.values().stream()
                .anyMatch(existingTransaction -> 
                        !existingTransaction.getId().equals(transaction.getId()) && 
                        (timeWindowSeconds <= 0 
                            ? existingTransaction.equalsForDuplication(transaction)
                            : existingTransaction.isPotentialDuplicate(transaction, timeWindowSeconds)));
    }
}
