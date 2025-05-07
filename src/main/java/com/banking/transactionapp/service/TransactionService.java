package com.banking.transactionapp.service;

import com.banking.transactionapp.dto.PageResponseDTO;
import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    
    TransactionResponseDTO createTransaction(TransactionCreateDTO createDTO);
    
    TransactionResponseDTO getTransactionById(UUID id);
    
    List<TransactionResponseDTO> getAllTransactions();
    
    PageResponseDTO<TransactionResponseDTO> getTransactionsPaginated(int page, int size);
    
    TransactionResponseDTO updateTransaction(UUID id, TransactionUpdateDTO updateDTO);
    
    void deleteTransaction(UUID id);
}
