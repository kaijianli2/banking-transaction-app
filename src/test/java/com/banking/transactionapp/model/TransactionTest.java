package com.banking.transactionapp.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void equalsForDuplication_SameTransaction() {
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        assertTrue(transaction1.equalsForDuplication(transaction1));
    }
    
    @Test
    void equalsForDuplication_DuplicateTransaction() {
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now)
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now.plusSeconds(5)) // Different timestamp
                .status(TransactionStatus.COMPLETED) // Different status
                .build();
        
        assertTrue(transaction1.equalsForDuplication(transaction2));
    }
    
    @Test
    void equalsForDuplication_DifferentTransaction() {
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00")) // Different amount
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        assertFalse(transaction1.equalsForDuplication(transaction2));
    }
    
    @Test
    void isPotentialDuplicate_SameTransaction() {
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        assertTrue(transaction1.isPotentialDuplicate(transaction1, 10));
    }
    
    @Test
    void isPotentialDuplicate_WithinTimeWindow() {
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now)
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now.plusSeconds(5)) // 5 seconds later
                .status(TransactionStatus.COMPLETED) // Different status
                .build();
        
        assertTrue(transaction1.isPotentialDuplicate(transaction2, 10));
    }
    
    @Test
    void isPotentialDuplicate_OutsideTimeWindow() {
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now)
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now.plusSeconds(15)) // 15 seconds later
                .status(TransactionStatus.PENDING)
                .build();
        
        assertFalse(transaction1.isPotentialDuplicate(transaction2, 10));
    }
    
    @Test
    void isPotentialDuplicate_DifferentAccount() {
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now)
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("987654321") // Different account
                .timestamp(now.plusSeconds(5))
                .status(TransactionStatus.PENDING)
                .build();
        
        assertFalse(transaction1.isPotentialDuplicate(transaction2, 10));
    }
    
    @Test
    void isPotentialDuplicate_DifferentAmount() {
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now)
                .status(TransactionStatus.PENDING)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00")) // Different amount
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(now.plusSeconds(5))
                .status(TransactionStatus.PENDING)
                .build();
        
        assertFalse(transaction1.isPotentialDuplicate(transaction2, 10));
    }
}
