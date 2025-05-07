package com.banking.transactionapp.dto;

import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    
    private UUID id;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private String accountNumber;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
