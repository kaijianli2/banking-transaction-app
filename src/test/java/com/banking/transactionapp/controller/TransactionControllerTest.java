package com.banking.transactionapp.controller;

import com.banking.transactionapp.dto.PageResponseDTO;
import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.dto.TransactionResponseDTO;
import com.banking.transactionapp.dto.TransactionUpdateDTO;
import com.banking.transactionapp.exception.DuplicateTransactionException;
import com.banking.transactionapp.exception.TransactionNotFoundException;
import com.banking.transactionapp.model.TransactionStatus;
import com.banking.transactionapp.model.TransactionType;
import com.banking.transactionapp.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private UUID transactionId;
    private TransactionResponseDTO responseDTO;
    private TransactionCreateDTO createDTO;
    private TransactionUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        
        responseDTO = TransactionResponseDTO.builder()
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
    void createTransaction_Success() throws Exception {
        when(transactionService.createTransaction(any(TransactionCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.accountNumber").value("123456789"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        verify(transactionService).createTransaction(any(TransactionCreateDTO.class));
    }

    @Test
    void createTransaction_ValidationFailure() throws Exception {
        TransactionCreateDTO invalidDTO = TransactionCreateDTO.builder()
                .amount(new BigDecimal("-100.00"))  // Invalid: negative amount
                .description("")  // Invalid: empty description
                .type(TransactionType.PAYMENT)
                .accountNumber("123456789")
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
        
        verify(transactionService, never()).createTransaction(any(TransactionCreateDTO.class));
    }

    @Test
    void createTransaction_DuplicateTransaction() throws Exception {
        when(transactionService.createTransaction(any(TransactionCreateDTO.class)))
                .thenThrow(new DuplicateTransactionException(10));

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A duplicate transaction was detected within 10 seconds"));
        
        verify(transactionService).createTransaction(any(TransactionCreateDTO.class));
    }

    @Test
    void getTransactionById_Success() throws Exception {
        when(transactionService.getTransactionById(transactionId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Test Transaction"));
        
        verify(transactionService).getTransactionById(transactionId);
    }

    @Test
    void getTransactionById_NotFound() throws Exception {
        when(transactionService.getTransactionById(transactionId))
                .thenThrow(new TransactionNotFoundException(transactionId));

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + transactionId));
        
        verify(transactionService).getTransactionById(transactionId);
    }

    @Test
    void getAllTransactions_Success() throws Exception {
        TransactionResponseDTO responseDTO2 = TransactionResponseDTO.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00"))
                .description("Another Transaction")
                .type(TransactionType.DEPOSIT)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        List<TransactionResponseDTO> transactions = Arrays.asList(responseDTO, responseDTO2);
        
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(transactionId.toString()))
                .andExpect(jsonPath("$[1].amount").value(200.0));
        
        verify(transactionService).getAllTransactions();
    }

    @Test
    void getTransactionsPaginated_Success() throws Exception {
        TransactionResponseDTO responseDTO2 = TransactionResponseDTO.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("200.00"))
                .description("Another Transaction")
                .type(TransactionType.DEPOSIT)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        List<TransactionResponseDTO> content = Arrays.asList(responseDTO, responseDTO2);
        
        PageResponseDTO<TransactionResponseDTO> pageResponse = PageResponseDTO.<TransactionResponseDTO>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        
        when(transactionService.getTransactionsPaginated(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/transactions/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
        
        verify(transactionService).getTransactionsPaginated(0, 10);
    }

    @Test
    void updateTransaction_Success() throws Exception {
        TransactionResponseDTO updatedResponseDTO = TransactionResponseDTO.builder()
                .id(transactionId)
                .amount(new BigDecimal("200.00"))
                .description("Updated Transaction")
                .type(TransactionType.TRANSFER)
                .accountNumber("987654321")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        when(transactionService.updateTransaction(eq(transactionId), any(TransactionUpdateDTO.class)))
                .thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(200.0))
                .andExpect(jsonPath("$.description").value("Updated Transaction"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.accountNumber").value("987654321"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        verify(transactionService).updateTransaction(eq(transactionId), any(TransactionUpdateDTO.class));
    }

    @Test
    void updateTransaction_NotFound() throws Exception {
        when(transactionService.updateTransaction(eq(transactionId), any(TransactionUpdateDTO.class)))
                .thenThrow(new TransactionNotFoundException(transactionId));

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + transactionId));
        
        verify(transactionService).updateTransaction(eq(transactionId), any(TransactionUpdateDTO.class));
    }

    @Test
    void deleteTransaction_Success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(transactionId);

        mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());
        
        verify(transactionService).deleteTransaction(transactionId);
    }

    @Test
    void deleteTransaction_NotFound() throws Exception {
        doThrow(new TransactionNotFoundException(transactionId))
                .when(transactionService).deleteTransaction(transactionId);

        mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: " + transactionId));
        
        verify(transactionService).deleteTransaction(transactionId);
    }
}
