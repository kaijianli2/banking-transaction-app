package com.banking.transactionapp.stress;

import com.banking.transactionapp.dto.TransactionCreateDTO;
import com.banking.transactionapp.model.TransactionType;
import com.banking.transactionapp.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class TransactionStressTest {

    @Autowired
    private TransactionService transactionService;
    
    private final Random random = new Random();
    
    // Common test parameters
    private static final int DEFAULT_NUM_THREADS = 10;
    private static final int DEFAULT_OPERATIONS_PER_THREAD = 100;
    private static final int DEFAULT_TIMEOUT_MINUTES = 2;
    
    @Test
    void stressTest_CreateTransactions() throws InterruptedException {
        int numThreads = 10;
        int numTransactionsPerThread = 100;
        int totalTransactions = numThreads * numTransactionsPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        log.info("Starting stress test with {} threads, {} transactions per thread", numThreads, numTransactionsPerThread);
        
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numTransactionsPerThread; j++) {
                        try {
                            TransactionCreateDTO createDTO = generateRandomTransaction();
                            // Add thread ID and iteration to make each transaction unique
                            createDTO.setDescription(createDTO.getDescription() + " - Thread " + Thread.currentThread().getId() + " - " + j);
                            transactionService.createTransaction(createDTO);
                            successCount.incrementAndGet();
                            // Add a small delay to avoid duplicate detection
                            Thread.sleep(5);
                        } catch (Exception e) {
                            log.error("Error creating transaction: {}", e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        
        double durationSeconds = (endTime - startTime) / 1000.0;
        double transactionsPerSecond = totalTransactions / durationSeconds;
        
        log.info("Stress test completed: {}", completed ? "SUCCESS" : "TIMEOUT");
        log.info("Total time: {} seconds", durationSeconds);
        log.info("Successful transactions: {}", successCount.get());
        log.info("Failed transactions: {}", errorCount.get());
        log.info("Transactions per second: {}", transactionsPerSecond);
    }
    
    @Test
    void stressTest_ReadTransactions() throws InterruptedException {
        // First create some transactions
        int numTransactionsToCreate = 100;
        List<UUID> transactionIds = new ArrayList<>();
        
        log.info("Creating {} transactions for read stress test", numTransactionsToCreate);
        
        for (int i = 0; i < numTransactionsToCreate; i++) {
            try {
                TransactionCreateDTO createDTO = generateRandomTransaction();
                // Add a unique identifier to avoid duplicates
                createDTO.setDescription(createDTO.getDescription() + " - " + UUID.randomUUID());
                UUID id = transactionService.createTransaction(createDTO).getId();
                transactionIds.add(id);
                // Add a small delay to avoid duplicate detection
                Thread.sleep(15);
            } catch (Exception e) {
                log.error("Error creating test transaction: {}", e.getMessage());
            }
        }
        
        int numThreads = 10;
        int numReadsPerThread = 1000;
        int totalReads = numThreads * numReadsPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        log.info("Starting read stress test with {} threads, {} reads per thread", numThreads, numReadsPerThread);
        
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numReadsPerThread; j++) {
                        try {
                            // Randomly choose between getting by ID or getting all
                            if (random.nextBoolean() && !transactionIds.isEmpty()) {
                                // Get by ID
                                UUID id = transactionIds.get(random.nextInt(transactionIds.size()));
                                transactionService.getTransactionById(id);
                            } else {
                                // Get all or paginated
                                if (random.nextBoolean()) {
                                    transactionService.getAllTransactions();
                                } else {
                                    int page = random.nextInt(5);
                                    int size = 10 + random.nextInt(10);
                                    transactionService.getTransactionsPaginated(page, size);
                                }
                            }
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Error reading transaction: {}", e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        
        double durationSeconds = (endTime - startTime) / 1000.0;
        double readsPerSecond = totalReads / durationSeconds;
        
        log.info("Read stress test completed: {}", completed ? "SUCCESS" : "TIMEOUT");
        log.info("Total time: {} seconds", durationSeconds);
        log.info("Successful reads: {}", successCount.get());
        log.info("Failed reads: {}", errorCount.get());
        log.info("Reads per second: {}", readsPerSecond);
    }
    
    @Test
    void stressTest_UpdateTransactions() throws InterruptedException {
        // First create some transactions
        int numTransactionsToCreate = 100;
        List<UUID> transactionIds = new ArrayList<>();
        
        log.info("Creating {} transactions for update stress test", numTransactionsToCreate);
        
        for (int i = 0; i < numTransactionsToCreate; i++) {
            try {
                TransactionCreateDTO createDTO = generateRandomTransaction();
                // Add a unique identifier to avoid duplicates
                createDTO.setDescription(createDTO.getDescription() + " - Update Test " + i);
                UUID id = transactionService.createTransaction(createDTO).getId();
                transactionIds.add(id);
                // Add a small delay to avoid duplicate detection
                Thread.sleep(15);
            } catch (Exception e) {
                log.error("Error creating test transaction: {}", e.getMessage());
            }
        }
        
        int numThreads = 10;
        int numUpdatesPerThread = 50;
        int totalUpdates = numThreads * numUpdatesPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        log.info("Starting update stress test with {} threads, {} updates per thread", numThreads, numUpdatesPerThread);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numUpdatesPerThread; j++) {
                        try {
                            if (!transactionIds.isEmpty()) {
                                // Get a random transaction ID
                                UUID id = transactionIds.get(random.nextInt(transactionIds.size()));
                                
                                // Create update DTO with unique description to avoid duplicates
                                var updateDTO = com.banking.transactionapp.dto.TransactionUpdateDTO.builder()
                                        .description("Updated by thread " + threadId + " - " + j + " - " + UUID.randomUUID())
                                        .amount(new BigDecimal(10 + random.nextInt(990)).setScale(2, BigDecimal.ROUND_HALF_UP))
                                        .build();
                                
                                transactionService.updateTransaction(id, updateDTO);
                                successCount.incrementAndGet();
                                
                                // Add a small delay to avoid duplicate detection
                                Thread.sleep(10);
                            }
                        } catch (Exception e) {
                            log.error("Error updating transaction: {}", e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        
        double durationSeconds = (endTime - startTime) / 1000.0;
        double updatesPerSecond = totalUpdates / durationSeconds;
        
        log.info("Update stress test completed: {}", completed ? "SUCCESS" : "TIMEOUT");
        log.info("Total time: {} seconds", durationSeconds);
        log.info("Successful updates: {}", successCount.get());
        log.info("Failed updates: {}", errorCount.get());
        log.info("Updates per second: {}", updatesPerSecond);
    }
    
    @Test
    void stressTest_DeleteTransactions() throws InterruptedException {
        // First create some transactions
        int numTransactionsToCreate = 100;
        List<UUID> transactionIds = new ArrayList<>();
        
        log.info("Creating {} transactions for delete stress test", numTransactionsToCreate);
        
        for (int i = 0; i < numTransactionsToCreate; i++) {
            try {
                TransactionCreateDTO createDTO = generateRandomTransaction();
                // Add a unique identifier to avoid duplicates
                createDTO.setDescription(createDTO.getDescription() + " - Delete Test " + i);
                UUID id = transactionService.createTransaction(createDTO).getId();
                transactionIds.add(id);
                // Add a small delay to avoid duplicate detection
                Thread.sleep(15);
            } catch (Exception e) {
                log.error("Error creating test transaction: {}", e.getMessage());
            }
        }
        
        int numThreads = 5;
        int numDeletesPerThread = 20;
        int totalDeletes = numThreads * numDeletesPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        log.info("Starting delete stress test with {} threads, {} deletes per thread", numThreads, numDeletesPerThread);
        
        // Divide the transaction IDs among the threads
        List<List<UUID>> partitionedIds = new ArrayList<>();
        int partitionSize = transactionIds.size() / numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            int startIndex = i * partitionSize;
            int endIndex = (i == numThreads - 1) ? transactionIds.size() : (i + 1) * partitionSize;
            partitionedIds.add(new ArrayList<>(transactionIds.subList(startIndex, endIndex)));
        }
        
        for (int i = 0; i < numThreads; i++) {
            final List<UUID> threadIds = partitionedIds.get(i);
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < Math.min(numDeletesPerThread, threadIds.size()); j++) {
                        try {
                            UUID id = threadIds.get(j);
                            transactionService.deleteTransaction(id);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Error deleting transaction: {}", e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        
        double durationSeconds = (endTime - startTime) / 1000.0;
        double deletesPerSecond = successCount.get() / durationSeconds;
        
        log.info("Delete stress test completed: {}", completed ? "SUCCESS" : "TIMEOUT");
        log.info("Total time: {} seconds", durationSeconds);
        log.info("Successful deletes: {}", successCount.get());
        log.info("Failed deletes: {}", errorCount.get());
        log.info("Deletes per second: {}", deletesPerSecond);
    }
    
    @Test
    void stressTest_MixedOperations() throws InterruptedException {
        // First create some transactions
        int numTransactionsToCreate = 50;
        List<UUID> transactionIds = new ArrayList<>();
        
        log.info("Creating {} transactions for mixed operations stress test", numTransactionsToCreate);
        
        for (int i = 0; i < numTransactionsToCreate; i++) {
            try {
                TransactionCreateDTO createDTO = generateRandomTransaction();
                // Add a unique identifier to avoid duplicates
                createDTO.setDescription(createDTO.getDescription() + " - Mixed Test " + i);
                UUID id = transactionService.createTransaction(createDTO).getId();
                transactionIds.add(id);
                // Add a small delay to avoid duplicate detection
                Thread.sleep(15);
            } catch (Exception e) {
                log.error("Error creating test transaction: {}", e.getMessage());
            }
        }
        
        int numThreads = 20;
        int numOperationsPerThread = 100;
        int totalOperations = numThreads * numOperationsPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger createCount = new AtomicInteger(0);
        AtomicInteger readCount = new AtomicInteger(0);
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger deleteCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        log.info("Starting mixed operations stress test with {} threads, {} operations per thread", 
                numThreads, numOperationsPerThread);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numOperationsPerThread; j++) {
                        try {
                            // Randomly choose operation type
                            int operationType = random.nextInt(10);
                            
                            if (operationType < 3) {
                                // 30% chance: Create
                                TransactionCreateDTO createDTO = generateRandomTransaction();
                                createDTO.setDescription(createDTO.getDescription() + " - Mixed Thread " + threadId + " - " + j);
                                UUID id = transactionService.createTransaction(createDTO).getId();
                                synchronized (transactionIds) {
                                    transactionIds.add(id);
                                }
                                createCount.incrementAndGet();
                                Thread.sleep(5);
                            } else if (operationType < 7) {
                                // 40% chance: Read
                                if (random.nextBoolean() && !transactionIds.isEmpty()) {
                                    // Get by ID
                                    UUID id;
                                    synchronized (transactionIds) {
                                        if (!transactionIds.isEmpty()) {
                                            id = transactionIds.get(random.nextInt(transactionIds.size()));
                                            transactionService.getTransactionById(id);
                                        }
                                    }
                                } else {
                                    // Get all or paginated
                                    if (random.nextBoolean()) {
                                        transactionService.getAllTransactions();
                                    } else {
                                        int page = random.nextInt(5);
                                        int size = 10 + random.nextInt(10);
                                        transactionService.getTransactionsPaginated(page, size);
                                    }
                                }
                                readCount.incrementAndGet();
                            } else if (operationType < 9) {
                                // 20% chance: Update
                                synchronized (transactionIds) {
                                    if (!transactionIds.isEmpty()) {
                                        UUID id = transactionIds.get(random.nextInt(transactionIds.size()));
                                        var updateDTO = com.banking.transactionapp.dto.TransactionUpdateDTO.builder()
                                                .description("Mixed Update " + threadId + "-" + j + "-" + UUID.randomUUID())
                                                .amount(new BigDecimal(10 + random.nextInt(990)).setScale(2, BigDecimal.ROUND_HALF_UP))
                                                .build();
                                        transactionService.updateTransaction(id, updateDTO);
                                        updateCount.incrementAndGet();
                                        Thread.sleep(5);
                                    }
                                }
                            } else {
                                // 10% chance: Delete
                                synchronized (transactionIds) {
                                    if (!transactionIds.isEmpty()) {
                                        int index = random.nextInt(transactionIds.size());
                                        UUID id = transactionIds.get(index);
                                        transactionService.deleteTransaction(id);
                                        transactionIds.remove(index);
                                        deleteCount.incrementAndGet();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error in mixed operation: {}", e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(3, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        
        double durationSeconds = (endTime - startTime) / 1000.0;
        double operationsPerSecond = (createCount.get() + readCount.get() + updateCount.get() + deleteCount.get()) / durationSeconds;
        
        log.info("Mixed operations stress test completed: {}", completed ? "SUCCESS" : "TIMEOUT");
        log.info("Total time: {} seconds", durationSeconds);
        log.info("Create operations: {}", createCount.get());
        log.info("Read operations: {}", readCount.get());
        log.info("Update operations: {}", updateCount.get());
        log.info("Delete operations: {}", deleteCount.get());
        log.info("Failed operations: {}", errorCount.get());
        log.info("Operations per second: {}", operationsPerSecond);
    }
    
    private TransactionCreateDTO generateRandomTransaction() {
        String[] descriptions = {
            "Grocery shopping", "Utility bill payment", "Salary deposit", 
            "Online purchase", "Restaurant payment", "Subscription fee",
            "Transfer to savings", "ATM withdrawal", "Loan payment",
            "Insurance premium"
        };
        
        // Add a timestamp to make each description unique
        String uniqueSuffix = " - " + System.currentTimeMillis() + "-" + random.nextInt(1000);
        
        String[] accountNumbers = {
            "1234567890", "2345678901", "3456789012", "4567890123", "5678901234"
        };
        
        TransactionType[] types = TransactionType.values();
        
        return TransactionCreateDTO.builder()
                .amount(new BigDecimal(10 + random.nextInt(990)).setScale(2, BigDecimal.ROUND_HALF_UP))
                .description(descriptions[random.nextInt(descriptions.length)] + uniqueSuffix)
                .type(types[random.nextInt(types.length)])
                .accountNumber(accountNumbers[random.nextInt(accountNumbers.length)])
                .build();
    }
}
