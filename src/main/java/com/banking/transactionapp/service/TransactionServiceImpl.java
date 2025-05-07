package com.banking.transactionapp.service;

import com.banking.transactionapp.config.CacheConfig;
import com.banking.transactionapp.dto.PageResponseDTO;
import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;
import com.banking.transactionapp.exception.DuplicateTransactionException;
import com.banking.transactionapp.exception.TransactionNotFoundException;
import com.banking.transactionapp.model.Transaction;
import com.banking.transactionapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    @Override
    @Caching(
        evict = { @CacheEvict(value = CacheConfig.TRANSACTIONS_CACHE, allEntries = true) },
        put = { @CachePut(value = CacheConfig.TRANSACTION_CACHE, key = "#result.id") }
    )
    public TransactionResponseDTO createTransaction(TransactionCreateDTO createDTO) {
        log.info("Creating new transaction");
        
        Transaction transaction = Transaction.builder()
                .amount(createDTO.getAmount())
                .description(createDTO.getDescription())
                .type(createDTO.getType())
                .accountNumber(createDTO.getAccountNumber())
                .build();
        
        // Check for duplicates within a 10-second window
        if (transactionRepository.isDuplicateWithinTimeWindow(transaction, 10)) {
            log.error("Duplicate transaction detected within 10-second window");
            throw new DuplicateTransactionException(10);
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", savedTransaction.getId());
        
        return mapToResponseDTO(savedTransaction);
    }
    
    @Override
    @Cacheable(value = CacheConfig.TRANSACTION_CACHE, key = "#id")
    public TransactionResponseDTO getTransactionById(UUID id) {
        log.info("Fetching transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", id);
                    return new TransactionNotFoundException(id);
                });
        
        return mapToResponseDTO(transaction);
    }
    
    @Override
    @Cacheable(value = CacheConfig.TRANSACTIONS_CACHE)
    public List<TransactionResponseDTO> getAllTransactions() {
        log.info("Fetching all transactions");
        
        return transactionRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponseDTO<TransactionResponseDTO> getTransactionsPaginated(int page, int size) {
        log.info("Fetching transactions page {} with size {}", page, size);
        
        List<TransactionResponseDTO> content = transactionRepository.findAll(page, size).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = transactionRepository.count();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PageResponseDTO.<TransactionResponseDTO>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page == totalPages - 1 || totalPages == 0)
                .build();
    }
    
    @Override
    @Caching(
        evict = { 
            @CacheEvict(value = CacheConfig.TRANSACTIONS_CACHE, allEntries = true)
        },
        put = { 
            @CachePut(value = CacheConfig.TRANSACTION_CACHE, key = "#id") 
        }
    )
    public TransactionResponseDTO updateTransaction(UUID id, TransactionUpdateDTO updateDTO) {
        log.info("Updating transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", id);
                    return new TransactionNotFoundException(id);
                });
        
        // Update fields if provided
        if (updateDTO.getAmount() != null) {
            transaction.setAmount(updateDTO.getAmount());
        }
        
        if (updateDTO.getDescription() != null) {
            transaction.setDescription(updateDTO.getDescription());
        }
        
        if (updateDTO.getType() != null) {
            transaction.setType(updateDTO.getType());
        }
        
        if (updateDTO.getAccountNumber() != null) {
            transaction.setAccountNumber(updateDTO.getAccountNumber());
        }
        
        if (updateDTO.getStatus() != null) {
            transaction.setStatus(updateDTO.getStatus());
        }
        
        // Check for duplicates within a 10-second window after update
        if (transactionRepository.isDuplicateWithinTimeWindow(transaction, 10)) {
            log.error("Update would create a duplicate transaction within 10-second window");
            throw new DuplicateTransactionException(10);
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction updated with ID: {}", updatedTransaction.getId());
        
        return mapToResponseDTO(updatedTransaction);
    }
    
    @Override
    @Caching(
        evict = { 
            @CacheEvict(value = CacheConfig.TRANSACTION_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.TRANSACTIONS_CACHE, allEntries = true)
        }
    )
    public void deleteTransaction(UUID id) {
        log.info("Deleting transaction with ID: {}", id);
        
        if (!transactionRepository.existsById(id)) {
            log.error("Transaction not found with ID: {}", id);
            throw new TransactionNotFoundException(id);
        }
        
        transactionRepository.deleteById(id);
        log.info("Transaction deleted with ID: {}", id);
    }
    
    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .accountNumber(transaction.getAccountNumber())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .build();
    }
}
