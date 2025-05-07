package com.banking.transactionapp.dto;

import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateDTO {
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
    
    private TransactionType type;
    
    private String accountNumber;
    
    private TransactionStatus status;
}
