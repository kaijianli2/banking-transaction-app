package com.banking.transactionapp.controller;

import com.banking.transactionapp.dto.PageResponseDTO;
import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;
import com.banking.transactionapp.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionCreateDTO createDTO) {
        log.info("REST request to create a new transaction");
        TransactionResponseDTO responseDTO = transactionService.createTransaction(createDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @PathVariable UUID id) {
        log.info("REST request to get transaction with ID: {}", id);
        TransactionResponseDTO responseDTO = transactionService.getTransactionById(id);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        log.info("REST request to get all transactions");
        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<PageResponseDTO<TransactionResponseDTO>> getTransactionsPaginated(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("REST request to get paginated transactions with page: {} and size: {}", page, size);
        PageResponseDTO<TransactionResponseDTO> pageResponse = 
                transactionService.getTransactionsPaginated(page, size);
        return ResponseEntity.ok(pageResponse);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateDTO updateDTO) {
        log.info("REST request to update transaction with ID: {}", id);
        TransactionResponseDTO responseDTO = transactionService.updateTransaction(id, updateDTO);
        return ResponseEntity.ok(responseDTO);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID id) {
        log.info("REST request to delete transaction with ID: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
