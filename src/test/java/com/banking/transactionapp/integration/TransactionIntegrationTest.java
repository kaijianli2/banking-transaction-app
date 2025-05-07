package com.banking.transactionapp.integration;

import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;
import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFullTransactionLifecycle() throws Exception {
        // 1. Create a transaction
        TransactionCreateDTO createDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("150.75"))
                .description("Integration Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("987654321")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.description").value("Integration Test Transaction"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.accountNumber").value("987654321"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        // Extract the created transaction ID
        String createResponseJson = createResult.getResponse().getContentAsString();
        TransactionResponseDTO createdTransaction = objectMapper.readValue(createResponseJson, TransactionResponseDTO.class);
        UUID transactionId = createdTransaction.getId();
        assertNotNull(transactionId);

        // 2. Get the transaction by ID
        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.description").value("Integration Test Transaction"));

        // 3. Get all transactions and verify our transaction is included
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + transactionId + "')]").exists());

        // 4. Get paginated transactions
        mockMvc.perform(get("/api/v1/transactions/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + transactionId + "')]").exists())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        // 5. Update the transaction
        TransactionUpdateDTO updateDTO = TransactionUpdateDTO.builder()
                .amount(new BigDecimal("200.50"))
                .description("Updated Integration Test Transaction")
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .build();

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(200.50))
                .andExpect(jsonPath("$.description").value("Updated Integration Test Transaction"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 6. Verify the update was persisted
        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.50))
                .andExpect(jsonPath("$.description").value("Updated Integration Test Transaction"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 7. Delete the transaction
        mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());

        // 8. Verify the transaction was deleted
        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDuplicateTransactionHandling() throws Exception {
        // Create a transaction
        TransactionCreateDTO createDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("100.00"))
                .description("Duplicate Test Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .build();

        // First creation should succeed
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());

        // Second creation with same details should fail as duplicate
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A duplicate transaction was detected within 10 seconds"));
    }

    @Test
    void testTransactionNotFoundHandling() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        // Get non-existent transaction
        mockMvc.perform(get("/api/v1/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + nonExistentId));

        // Update non-existent transaction
        TransactionUpdateDTO updateDTO = TransactionUpdateDTO.builder()
                .amount(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(put("/api/v1/transactions/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + nonExistentId));

        // Delete non-existent transaction
        mockMvc.perform(delete("/api/v1/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + nonExistentId));
    }

    @Test
    void testValidationErrors() throws Exception {
        // Test with invalid amount (negative)
        TransactionCreateDTO invalidAmountDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("-50.00"))
                .description("Invalid Transaction")
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAmountDTO)))
                .andExpect(status().isBadRequest());

        // Test with missing required fields
        TransactionCreateDTO missingFieldsDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("100.00"))
                // Missing description
                .type(TransactionType.PAYMENT)
                // Missing account number
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingFieldsDTO)))
                .andExpect(status().isBadRequest());
    }
}
