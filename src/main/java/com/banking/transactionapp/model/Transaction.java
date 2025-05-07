package com.banking.transactionapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class Transaction {
    
    @Builder.Default
    private UUID id = UUID.randomUUID();
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    // Custom equals method to check for duplicate transactions
    public boolean equalsForDuplication(Transaction other) {
        
        if (this == other) return true;
        if (other == null) return false;
        
        return amount.equals(other.amount) &&
               description.equals(other.description) &&
               type == other.type &&
               accountNumber.equals(other.accountNumber);
    }
    
    /**
     * Checks if this transaction is a potential duplicate of another transaction
     * for the same customer within the specified time window in seconds.
     * 
     * @param other The other transaction to compare with
     * @param timeWindowSeconds The time window in seconds
     * @return true if the transactions are potential duplicates, false otherwise
     */
    public boolean isPotentialDuplicate(Transaction other, long timeWindowSeconds) {
        if (this == other) return true;
        if (other == null) return false;
        
        // Check if it's the same customer (account number)
        if (!accountNumber.equals(other.accountNumber)) {
            return false;
        }
        
        // Check if amount, description, and type match
        boolean basicFieldsMatch = amount.equals(other.amount) &&
                                  description.equals(other.description) &&
                                  type == other.type;
        
        if (!basicFieldsMatch) {
            return false;
        }
        
        // Check if the transactions are within the specified time window
        long secondsBetween = Math.abs(
            java.time.Duration.between(timestamp, other.timestamp).getSeconds()
        );
        
        return secondsBetween <= timeWindowSeconds;
    }
}
