package com.banking.transactionapp.dto;

import com.banking.transactionapp.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TransactionCreateDTO {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
}
