package com.banking.transactionapp.repository;

import com.banking.transactionapp.model.Transaction;
import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repository;
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        
        // Create a base transaction
        transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        // Save the transaction to the repository
        repository.save(transaction1);
        
        // Create a similar transaction with a different ID but same details
        transaction2 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now().plusSeconds(5)) // 5 seconds later
                .status(TransactionStatus.PENDING)
                .build();
        
        // Create a different transaction
        transaction3 = Transaction.builder()
                .amount(new BigDecimal("200.00"))
                .description("Another Transaction")
                .type(TransactionType.DEPOSIT)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    void save_Success() {
        Transaction savedTransaction = repository.save(transaction3);
        
        assertEquals(transaction3.getId(), savedTransaction.getId());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findById_Success() {
        Optional<Transaction> found = repository.findById(transaction1.getId());
        
        assertTrue(found.isPresent());
        assertEquals(transaction1.getId(), found.get().getId());
    }

    @Test
    void findById_NotFound() {
        Optional<Transaction> found = repository.findById(UUID.randomUUID());
        
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_Success() {
        repository.save(transaction3);
        
        List<Transaction> transactions = repository.findAll();
        
        assertEquals(2, transactions.size());
        assertTrue(transactions.contains(transaction1));
        assertTrue(transactions.contains(transaction3));
    }
    
    @Test
    void findAll_Paginated_Success() {
        repository.save(transaction2);
        repository.save(transaction3);
        
        List<Transaction> transactions = repository.findAll(0, 2);
        
        assertEquals(2, transactions.size());
    }
    
    @Test
    void count_Success() {
        repository.save(transaction2);
        repository.save(transaction3);
        
        assertEquals(3, repository.count());
    }
    
    @Test
    void deleteById_Success() {
        repository.deleteById(transaction1.getId());
        
        assertFalse(repository.existsById(transaction1.getId()));
        assertEquals(0, repository.count());
    }
    
    @Test
    void existsById_Success() {
        assertTrue(repository.existsById(transaction1.getId()));
        assertFalse(repository.existsById(UUID.randomUUID()));
    }
    
    @Test
    void isDuplicate_ExactMatch() {
        // Should detect as duplicate (exact match except ID)
        assertTrue(repository.isDuplicate(transaction2));
        
        // Should not detect as duplicate (different details)
        assertFalse(repository.isDuplicate(transaction3));
        
        // Should not detect itself as duplicate
        assertFalse(repository.isDuplicate(transaction1));
    }
    
    @Test
    void isDuplicateWithinTimeWindow_SameTransaction() {
        // Should not detect itself as duplicate
        assertFalse(repository.isDuplicateWithinTimeWindow(transaction1, 10));
    }
    
    @Test
    void isDuplicateWithinTimeWindow_WithinWindow() {
        // Should detect as duplicate (within 10 seconds)
        assertTrue(repository.isDuplicateWithinTimeWindow(transaction2, 10));
    }
    
    @Test
    void isDuplicateWithinTimeWindow_OutsideWindow() {
        // Create a transaction with timestamp 15 seconds later
        Transaction laterTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now().plusSeconds(15))
                .status(TransactionStatus.PENDING)
                .build();
        
        // Should not detect as duplicate (outside 10 second window)
        assertFalse(repository.isDuplicateWithinTimeWindow(laterTransaction, 10));
    }
    
    @Test
    void isDuplicateWithinTimeWindow_DifferentAccount() {
        // Create a transaction with different account number
        Transaction differentAccountTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("987654321") // Different account
                .timestamp(LocalDateTime.now().plusSeconds(5))
                .status(TransactionStatus.PENDING)
                .build();
        
        // Should not detect as duplicate (different account)
        assertFalse(repository.isDuplicateWithinTimeWindow(differentAccountTransaction, 10));
    }
}
