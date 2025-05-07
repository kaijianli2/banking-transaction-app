# Banking Transaction Management Application

A Spring Boot application for managing banking transactions with a focus on performance, reliability, and scalability. This application provides a RESTful API for creating, reading, updating, and deleting financial transactions with robust validation, error handling, and performance optimizations.

## Features

- RESTful API for transaction management
- In-memory transaction storage with thread-safe implementation
- Intelligent duplicate transaction detection
- Two-level caching strategy with Caffeine
- Comprehensive validation and error handling
- Efficient pagination for large datasets
- Containerization with Docker and Kubernetes
- Comprehensive testing suite (unit, integration, and stress tests)
- Performance monitoring via Spring Actuator

## Architecture

The application follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and transaction processing
- **Repository Layer**: Manages data access and storage
- **Model Layer**: Defines the domain entities
- **DTO Layer**: Data transfer objects for API communication
- **Exception Layer**: Custom exceptions and global exception handling

### Key Components

- **Transaction**: Core domain entity representing a financial transaction
- **TransactionController**: REST API endpoints for transaction management
- **TransactionService**: Business logic for transaction processing
- **InMemoryTransactionRepository**: Thread-safe in-memory data store
- **CacheConfig**: Configuration for two-level caching system
- **GlobalExceptionHandler**: Centralized exception handling

## Tech Stack

- Java 21
- Spring Boot 3.2.0
- Spring Web (RESTful API)
- Spring Validation (Input validation)
- Spring Cache with Caffeine (High-performance caching)
- Spring Actuator (Monitoring and metrics)
- Lombok (Boilerplate code reduction)
- JUnit 5 & Mockito (Testing)
- Docker & Docker Compose (Containerization)
- Kubernetes (Orchestration)

## API Endpoints

### Create Transaction
```
POST /api/v1/transactions
```
Request body:
```json
{
  "amount": 100.00,
  "description": "Grocery shopping",
  "type": "PAYMENT",
  "accountNumber": "1234567890"
}
```
Response (201 Created):
```json
{
  "id": "eb3fba4e-8d11-466a-bbb6-6be6060b7802",
  "amount": 100.00,
  "description": "Grocery shopping",
  "type": "PAYMENT",
  "accountNumber": "1234567890",
  "timestamp": "2025-05-07T14:30:00",
  "status": "PENDING"
}
```

### Get Transaction by ID
```
GET /api/v1/transactions/{id}
```
Response (200 OK):
```json
{
  "id": "eb3fba4e-8d11-466a-bbb6-6be6060b7802",
  "amount": 100.00,
  "description": "Grocery shopping",
  "type": "PAYMENT",
  "accountNumber": "1234567890",
  "timestamp": "2025-05-07T14:30:00",
  "status": "PENDING"
}
```

### Get All Transactions
```
GET /api/v1/transactions
```
Response (200 OK):
```json
[
  {
    "id": "eb3fba4e-8d11-466a-bbb6-6be6060b7802",
    "amount": 100.00,
    "description": "Grocery shopping",
    "type": "PAYMENT",
    "accountNumber": "1234567890",
    "timestamp": "2025-05-07T14:30:00",
    "status": "PENDING"
  },
  {
    "id": "7c9e6b5a-4d33-422a-8ff9-1a2b3c4d5e6f",
    "amount": 200.00,
    "description": "Utility bill payment",
    "type": "PAYMENT",
    "accountNumber": "0987654321",
    "timestamp": "2025-05-07T14:35:00",
    "status": "COMPLETED"
  }
]
```

### Get Paginated Transactions
```
GET /api/v1/transactions/paged?page=0&size=10
```
Response (200 OK):
```json
{
  "content": [
    {
      "id": "eb3fba4e-8d11-466a-bbb6-6be6060b7802",
      "amount": 100.00,
      "description": "Grocery shopping",
      "type": "PAYMENT",
      "accountNumber": "1234567890",
      "timestamp": "2025-05-07T14:30:00",
      "status": "PENDING"
    },
    {
      "id": "7c9e6b5a-4d33-422a-8ff9-1a2b3c4d5e6f",
      "amount": 200.00,
      "description": "Utility bill payment",
      "type": "PAYMENT",
      "accountNumber": "0987654321",
      "timestamp": "2025-05-07T14:35:00",
      "status": "COMPLETED"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Update Transaction
```
PUT /api/v1/transactions/{id}
```
Request body:
```json
{
  "amount": 200.00,
  "description": "Updated description",
  "type": "TRANSFER",
  "accountNumber": "0987654321",
  "status": "COMPLETED"
}
```
Response (200 OK):
```json
{
  "id": "eb3fba4e-8d11-466a-bbb6-6be6060b7802",
  "amount": 200.00,
  "description": "Updated description",
  "type": "TRANSFER",
  "accountNumber": "0987654321",
  "timestamp": "2025-05-07T14:30:00",
  "status": "COMPLETED"
}
```

### Delete Transaction
```
DELETE /api/v1/transactions/{id}
```
Response (204 No Content)

### Error Responses

#### Transaction Not Found (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found with id: eb3fba4e-8d11-466a-bbb6-6be6060b7802",
  "path": "/api/v1/transactions/eb3fba4e-8d11-466a-bbb6-6be6060b7802",
  "timestamp": "2025-05-07T14:40:00"
}
```

#### Duplicate Transaction (409 Conflict)
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A duplicate transaction was detected within 10 seconds",
  "path": "/api/v1/transactions",
  "timestamp": "2025-05-07T14:40:00"
}
```

#### Validation Error (400 Bad Request)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: {amount=Amount must be positive, description=Description cannot be empty}",
  "path": "/api/v1/transactions",
  "timestamp": "2025-05-07T14:40:00"
}
```

## Project Structure

```
banking-transaction-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── banking/
│   │   │           └── transactionapp/
│   │   │               ├── config/           # Configuration classes
│   │   │               ├── controller/       # REST controllers
│   │   │               ├── dto/              # Data Transfer Objects
│   │   │               ├── exception/        # Custom exceptions
│   │   │               ├── model/            # Domain models
│   │   │               ├── repository/       # Data repositories
│   │   │               ├── service/          # Business services
│   │   │               └── TransactionApplication.java
│   │   └── resources/
│   │       └── application.yml               # Application configuration
│   └── test/
│       └── java/
│           └── com/
│               └── banking/
│                   └── transactionapp/
│                       ├── cache/            # Cache tests
│                       ├── controller/       # Controller tests
│                       ├── exception/        # Exception handler tests
│                       ├── integration/      # Integration tests
│                       ├── model/            # Model tests
│                       ├── repository/       # Repository tests
│                       ├── service/          # Service tests
│                       └── stress/           # Stress tests
├── kubernetes/                               # Kubernetes configuration
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Prerequisites

- Java 21
- Maven 3.8+
- Docker (optional)
- Kubernetes (optional)

### Running Locally

```bash
# Clone the repository
git clone https://github.com/kaijianli2/banking-transaction-app.git
cd banking-transaction-app

# Build the application
mvn clean package

# Run the application
java -jar target/transaction-app-0.0.1-SNAPSHOT.jar
```

The application will be available at http://localhost:8080

### Running with Docker

```bash
# Build the Docker image
docker build -t banking-transaction-app .

# Run the container
docker run -p 8080:8080 banking-transaction-app
```

### Running with Docker Compose

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

### Deploying to Kubernetes

```bash
# Apply the Kubernetes deployment
kubectl apply -f kubernetes/deployment.yaml

# Check deployment status
kubectl get deployments

# Check pods
kubectl get pods

# Access the service
kubectl port-forward svc/banking-transaction-app 8080:8080
```

## Key Features in Detail

### Duplicate Transaction Detection

The application implements an intelligent duplicate transaction detection mechanism to prevent accidental duplicate submissions:

- Transactions are considered duplicates if they have the same amount, description, type, and account number
- A time window (configurable, default 10 seconds) is used to detect potential duplicates
- The `equalsForDuplication()` method in the Transaction class handles the comparison logic
- The repository layer implements the detection with the `isDuplicateWithinTimeWindow()` method

### Caching Implementation

A two-level caching strategy is implemented for optimal performance:

- **Individual Transaction Cache**: Caches transactions by ID for fast retrieval
- **All Transactions Cache**: Caches the complete list of transactions
- Cache eviction policies ensure data consistency when transactions are modified
- Cache configuration is centralized in the `CacheConfig` class
- Caffeine is used as the caching provider for high performance

### Testing

#### Unit Tests
- Controller tests with MockMvc
- Service tests with Mockito
- Repository tests for data access logic
- Model tests for business logic
- Exception handler tests for error responses

#### Integration Tests
- End-to-end tests covering the full transaction lifecycle
- Tests for error handling in an integrated environment
- Validation tests with real HTTP requests

#### Stress Tests
- Create operations stress test: Tests high-volume transaction creation
- Read operations stress test: Tests high-volume transaction retrieval
- Update operations stress test: Tests concurrent transaction updates
- Delete operations stress test: Tests concurrent transaction deletion
- Mixed operations stress test: Tests a mix of all operations under load

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dgroups="UnitTest"

# Run only integration tests
mvn test -Dgroups="IntegrationTest"

# Run only stress tests
mvn test -Dtest=TransactionStressTest
```

## External Libraries

- **Spring Boot Starter Web**: For building the RESTful API
- **Spring Boot Starter Validation**: For input validation using Bean Validation (JSR-380)
- **Spring Boot Starter Cache**: For declarative caching support
- **Spring Boot Starter Actuator**: For monitoring and health checks
- **Caffeine**: For high-performance, near-optimal caching
- **Lombok**: For reducing boilerplate code through annotations
- **Spring Boot Starter Test**: For comprehensive testing support
- **JUnit 5**: For unit and integration testing
- **Mockito**: For mocking dependencies in tests

## Performance Considerations

- **In-memory storage**: Fast data access with O(1) lookup time
- **Two-level caching**: Reduces database hits and improves response times
- **Pagination**: Efficient handling of large data sets with memory optimization
- **Thread safety**: Concurrent transaction processing with synchronized collections
- **Optimistic locking**: Prevents data corruption during concurrent updates
- **Horizontal scaling**: Kubernetes deployment for distributing load across multiple instances
- **Performance metrics**: Real-time monitoring via Spring Actuator

## Error Handling

The application implements a comprehensive error handling strategy:

- **GlobalExceptionHandler**: Centralized exception handling for consistent responses
- **Custom exceptions**: Domain-specific exceptions for clear error identification
    - TransactionNotFoundException: When a transaction cannot be found
    - DuplicateTransactionException: When a duplicate transaction is detected
- **Validation errors**: Automatic validation using Bean Validation with custom error messages
- **Standardized error responses**: Consistent ErrorResponseDTO format for all errors
- **Appropriate HTTP status codes**:
    - 400 Bad Request: For validation errors
    - 404 Not Found: For non-existent transactions
    - 409 Conflict: For duplicate transactions
    - 500 Internal Server Error: For unexpected errors

## Monitoring and Observability

The application provides comprehensive monitoring capabilities via Spring Actuator:

- **Health checks**: `/actuator/health` for application health status
- **Metrics**: `/actuator/metrics` for performance metrics
    - JVM metrics (memory, threads, garbage collection)
    - HTTP request metrics (count, timing)
    - Cache metrics (hits, misses, evictions)
- **Cache statistics**: `/actuator/caches` for detailed cache information
- **Environment**: `/actuator/env` for configuration information
- **Logging**: `/actuator/loggers` for runtime log level management

These endpoints can be integrated with monitoring tools like Prometheus and Grafana for visualization and alerting.

## Future Improvements

- **Persistent storage**: Add database support (PostgreSQL, MongoDB)
- **Authentication and authorization**: Implement Spring Security with JWT
- **Advanced filtering**: Add query parameters for filtering transactions
- **Full-text search**: Implement Elasticsearch for transaction searching
- **Event-driven architecture**: Use Spring Cloud Stream for asynchronous processing
- **API documentation**: Add Swagger/OpenAPI for interactive documentation
- **Rate limiting**: Implement API rate limiting for abuse prevention
- **Circuit breaker**: Add resilience patterns for external service calls
- **Distributed tracing**: Implement with Spring Cloud Sleuth and Zipkin
- **Internationalization**: Support for multiple languages in error messages
