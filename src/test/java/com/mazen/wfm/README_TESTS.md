# Project Management Test Suite

This directory contains comprehensive unit and integration tests for the Project Management functionality in the WFM (Workflow Management) application.

## Test Structure

### 1. Repository Layer Tests

- **`ProjectRepositoryUnitTest.java`** - Unit tests for ProjectRepository
  - Tests CRUD operations
  - Tests custom query methods (`findByOwner_UserId`, `findByNameContainingIgnoreCase`)
  - Tests edge cases and error conditions
  - Uses `@DataJpaTest` for isolated JPA testing

### 2. Service Layer Tests

- **`ProjectServiceIntegrationTest.java`** - Integration tests for ProjectService
  - Tests business logic with real database interactions
  - Tests authorization and security constraints
  - Tests transaction boundaries
  - Uses `@SpringBootTest` for full application context

### 3. Controller Layer Tests

#### Unit Tests

- **`ProjectControllerUnitTest.java`** - Unit tests for ProjectController
  - Tests controller methods in isolation
  - Uses Mockito to mock dependencies
  - Tests HTTP response mapping and status codes
  - Tests exception handling

#### Integration Tests

- **`ProjectControllerIntegrationTest.java`** - Integration tests for ProjectController
  - Tests full HTTP layer with MockMvc
  - Tests JSON serialization/deserialization
  - Tests security configuration
  - Tests validation and error responses

### 4. Mapper Tests

- **`ProjectMapperTest.java`** - Unit tests for ProjectMapper
  - Tests MapStruct mapping configurations
  - Tests entity to DTO conversions
  - Tests update operations with null handling

## Test Configuration

### Test Properties

- **`application-test.properties`** - Test-specific configuration
  - Uses H2 in-memory database
  - Configures JPA for testing
  - Sets up security configuration
  - Enables debug logging

### Test Data Setup

All tests use `@BeforeEach` methods to set up clean test data:

- Creates test users with different roles
- Creates test projects with various states
- Ensures data isolation between tests

## Running the Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Classes

```bash
# Repository tests only
mvn test -Dtest=ProjectRepositoryUnitTest

# Service integration tests only
mvn test -Dtest=ProjectServiceIntegrationTest

# Controller unit tests only
mvn test -Dtest=ProjectControllerUnitTest

# Controller integration tests only
mvn test -Dtest=ProjectControllerIntegrationTest

# Mapper tests only
mvn test -Dtest=ProjectMapperTest
```

### Run Test Suite

```bash
mvn test -Dtest=ProjectTestSuite
```

### Run Tests with Coverage

```bash
mvn test jacoco:report
```

## Test Coverage

The test suite covers:

### Repository Layer

- ✅ Basic CRUD operations
- ✅ Custom query methods
- ✅ Edge cases (empty results, null parameters)
- ✅ Data persistence and retrieval

### Service Layer

- ✅ Business logic validation
- ✅ Authorization checks
- ✅ Transaction management
- ✅ Exception handling
- ✅ Data transformation

### Controller Layer

- ✅ HTTP endpoint mapping
- ✅ Request/response handling
- ✅ Status code validation
- ✅ JSON serialization
- ✅ Security integration
- ✅ Validation error handling

### Mapper Layer

- ✅ Entity to DTO mapping
- ✅ DTO to Entity mapping
- ✅ Update operations
- ✅ Null value handling
- ✅ Field mapping accuracy

## Test Data Patterns

### Test Users

- **Regular User**: `testuser` with `USER` role
- **Admin User**: `admin` with `ADMIN` role
- **Different Users**: For testing authorization

### Test Projects

- **Basic Project**: Standard project with all fields
- **Minimal Project**: Project with only required fields
- **Projects with Different Owners**: For testing ownership validation

## Assertions and Validations

### Repository Tests

- Data persistence verification
- Query result accuracy
- Empty result handling
- Exception throwing

### Service Tests

- Business logic correctness
- Authorization enforcement
- Transaction rollback on errors
- Data integrity maintenance

### Controller Tests

- HTTP status code validation
- Response body structure validation
- Error response format validation
- Security constraint enforcement

### Mapper Tests

- Field mapping accuracy
- Null value handling
- Type conversion correctness
- Update operation behavior

## Best Practices Implemented

1. **Test Isolation**: Each test is independent and doesn't affect others
2. **Clean Setup**: Fresh test data for each test method
3. **Comprehensive Coverage**: Tests cover happy path, edge cases, and error conditions
4. **Realistic Test Data**: Test data mirrors production scenarios
5. **Clear Assertions**: Descriptive assertions with meaningful error messages
6. **Proper Mocking**: Appropriate use of mocks vs real objects
7. **Transaction Management**: Proper handling of test transactions
8. **Security Testing**: Authentication and authorization validation

## Maintenance Notes

- When adding new fields to entities, update corresponding test data setup
- When modifying business logic, ensure all test scenarios are covered
- When changing API contracts, update controller tests accordingly
- When adding new repository methods, add corresponding unit tests
- When modifying security configuration, update integration tests

## Troubleshooting

### Common Issues

1. **H2 Database Issues**: Ensure `application-test.properties` is properly configured
2. **Transaction Rollback**: Use `@Transactional` on test methods for proper cleanup
3. **Security Context**: Use `@WithMockUser` for authentication in tests
4. **MockMvc Configuration**: Ensure proper security configuration is applied

### Debug Tips

- Enable debug logging in test properties
- Use H2 console for database inspection during tests
- Check test data setup in `@BeforeEach` methods
- Verify mock configurations in unit tests
