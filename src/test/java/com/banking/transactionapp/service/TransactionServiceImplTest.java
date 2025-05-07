package com.banking.transactionapp.service;

import com.banking.transactionapp.dto.PageResponseDTO;
import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;
import com.banking.transactionapp.exception.DuplicateTransactionException;
import com.banking.transactionapp.exception.TransactionNotFoundException;
import com.banking.transactionapp.model.Transaction;
import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import com.banking.transactionapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID transactionId;
    private Transaction transaction;
    private TransactionCreateDTO createDTO;
    private TransactionUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        
        transaction = Transaction.builder()
                .id(transactionId)
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        createDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("100.00"))
                .description("Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .build();
        
        updateDTO = TransactionUpdateDTO.builder()
                .amount(new BigDecimal("200.00"))
                .description("Updated Transaction")
                .type(TransactionType.TRANSFER)
                .accountNumber("987654321")
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    void createTransaction_Success() {
        when(transactionRepository.isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L))).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDTO result = transactionService.createTransaction(createDTO);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getAmount(), result.getAmount());
        assertEquals(transaction.getDescription(), result.getDescription());
        assertEquals(transaction.getType(), result.getType());
        assertEquals(transaction.getAccountNumber(), result.getAccountNumber());
        assertEquals(transaction.getStatus(), result.getStatus());
        
        verify(transactionRepository).isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_DuplicateTransaction() {
        when(transactionRepository.isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L))).thenReturn(true);

        DuplicateTransactionException exception = assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.createTransaction(createDTO);
        });
        
        assertEquals("A duplicate transaction was detected within 10 seconds", exception.getMessage());
        verify(transactionRepository).isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        TransactionResponseDTO result = transactionService.getTransactionById(transactionId);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getAmount(), result.getAmount());
        assertEquals(transaction.getDescription(), result.getDescription());
        
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById(transactionId);
        });
        
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void getAllTransactions_Success() {
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00"))
                .description("Another Transaction")
                .type(TransactionType.DEPOSIT)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(transaction, transaction2));

        List<TransactionResponseDTO> results = transactionService.getAllTransactions();

        assertNotNull(results);
        assertEquals(2, results.size());
        
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionsPaginated_Success() {
        int page = 0;
        int size = 10;
        long totalElements = 2;
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00"))
                .description("Another Transaction")
                .type(TransactionType.DEPOSIT)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        when(transactionRepository.findAll(page, size)).thenReturn(Arrays.asList(transaction, transaction2));
        when(transactionRepository.count()).thenReturn(totalElements);

        PageResponseDTO<TransactionResponseDTO> result = transactionService.getTransactionsPaginated(page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(page, result.getPageNumber());
        assertEquals(size, result.getPageSize());
        assertEquals(totalElements, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        
        verify(transactionRepository).findAll(page, size);
        verify(transactionRepository).count();
    }

    @Test
    void updateTransaction_Success() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L))).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDTO result = transactionService.updateTransaction(transactionId, updateDTO);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_NotFound() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.updateTransaction(transactionId, updateDTO);
        });
        
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).isDuplicateWithinTimeWindow(any(Transaction.class), anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_DuplicateTransaction() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L))).thenReturn(true);

        DuplicateTransactionException exception = assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.updateTransaction(transactionId, updateDTO);
        });
        
        assertEquals("A duplicate transaction was detected within 10 seconds", exception.getMessage());
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).isDuplicateWithinTimeWindow(any(Transaction.class), eq(10L));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(transactionId);

        transactionService.deleteTransaction(transactionId);
        
        verify(transactionRepository).existsById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void deleteTransaction_NotFound() {
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteTransaction(transactionId);
        });
        
        verify(transactionRepository).existsById(transactionId);
        verify(transactionRepository, never()).deleteById(any(UUID.class));
    }
}
