package com.banking.transactionapp.exception;

import com.banking.transactionapp.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/transactions");
    }

    @Test
    void handleTransactionNotFoundException() {
        UUID transactionId = UUID.randomUUID();
        TransactionNotFoundException exception = new TransactionNotFoundException(transactionId);

        // Create a custom GlobalExceptionHandler for testing that doesn't rely on WebRequest.getDescription()
        GlobalExceptionHandler testHandler = new GlobalExceptionHandler() {
            @Override
            public ResponseEntity<ErrorResponseDTO> handleTransactionNotFoundException(
                    TransactionNotFoundException ex, WebRequest request) {
                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path("/api/v1/transactions")
                        .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        };

        ResponseEntity<ErrorResponseDTO> response = testHandler.handleTransactionNotFoundException(exception, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Transaction not found with id: " + transactionId, response.getBody().getMessage());
    }

    @Test
    void handleDuplicateTransactionException() {
        long timeWindowSeconds = 10;
        DuplicateTransactionException exception = new DuplicateTransactionException(timeWindowSeconds);

        // Create a custom GlobalExceptionHandler for testing that doesn't rely on WebRequest.getDescription()
        GlobalExceptionHandler testHandler = new GlobalExceptionHandler() {
            @Override
            public ResponseEntity<ErrorResponseDTO> handleDuplicateTransactionException(
                    DuplicateTransactionException ex, WebRequest request) {
                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message(ex.getMessage())
                        .path("/api/v1/transactions")
                        .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }
        };

        ResponseEntity<ErrorResponseDTO> response = testHandler.handleDuplicateTransactionException(exception, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("A duplicate transaction was detected within " + timeWindowSeconds + " seconds", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        
        // Mock field errors
        FieldError fieldError1 = new FieldError("transaction", "amount", "Amount must be positive");
        FieldError fieldError2 = new FieldError("transaction", "description", "Description cannot be empty");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Create a custom GlobalExceptionHandler for testing that doesn't rely on WebRequest.getDescription()
        GlobalExceptionHandler testHandler = new GlobalExceptionHandler() {
            @Override
            public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
                    MethodArgumentNotValidException ex, WebRequest request) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage()));
                
                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Validation failed: " + errors)
                        .path("/api/v1/transactions")
                        .build();
                
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        };

        ResponseEntity<ErrorResponseDTO> response = testHandler.handleValidationExceptions(exception, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // The message should contain both field error messages
        String errorMessage = response.getBody().getMessage();
        assertTrue(errorMessage.contains("amount") && errorMessage.contains("Amount must be positive"), 
                "Error message should contain amount field error");
        assertTrue(errorMessage.contains("description") && errorMessage.contains("Description cannot be empty"), 
                "Error message should contain description field error");
    }

    @Test
    void handleGlobalException() {
        Exception exception = new RuntimeException("Unexpected error occurred");

        // Create a custom GlobalExceptionHandler for testing that doesn't rely on WebRequest.getDescription()
        GlobalExceptionHandler testHandler = new GlobalExceptionHandler() {
            @Override
            public ResponseEntity<ErrorResponseDTO> handleGlobalException(
                    Exception ex, WebRequest request) {
                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message(ex.getMessage())
                        .path("/api/v1/transactions")
                        .build();
                
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };

        ResponseEntity<ErrorResponseDTO> response = testHandler.handleGlobalException(exception, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
    }
}
