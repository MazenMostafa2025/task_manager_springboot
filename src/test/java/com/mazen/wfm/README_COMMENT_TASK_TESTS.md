# Comment and Task Management Test Suite

This directory contains comprehensive unit and integration tests for the Comment and Task Management functionality in the WFM (Workflow Management) application.

## Test Structure

### 1. Repository Layer Tests

#### Comment Repository Tests

- **`CommentRepositoryUnitTest.java`** - Unit tests for CommentRepository
  - Tests CRUD operations for comments
  - Tests custom query methods (`findByTask_TaskId`, `findByAuthor_UserId`)
  - Tests edge cases (empty results, null parameters, timestamp handling)
  - Uses `@DataJpaTest` for isolated JPA testing

#### Task Repository Tests

- **`TaskRepositoryUnitTest.java`** - Unit tests for TaskRepository
  - Tests CRUD operations for tasks
  - Tests custom query methods (`findByProject_ProjectId`, `findByAssignees_UserId`, `findByStatus`, etc.)
  - Tests pagination support
  - Tests complex queries with multiple parameters
  - Tests edge cases and data validation

### 2. Service Layer Tests

#### Comment Service Tests

- **`CommentServiceIntegrationTest.java`** - Integration tests for CommentService
  - Tests business logic with real database interactions
  - Tests authorization and security constraints (author vs admin vs unauthorized)
  - Tests transaction management and data integrity
  - Tests all service methods including create, read, delete operations
  - Uses `@SpringBootTest` for full application context

#### Task Service Tests

- **`TaskServiceIntegrationTest.java`** - Integration tests for TaskService
  - Tests business logic with real database interactions
  - Tests complex operations (user assignment, tag assignment, status filtering)
  - Tests pagination and filtering functionality
  - Tests transaction management and data integrity
  - Tests all service methods including CRUD, assignment, and filtering operations

### 3. Controller Layer Tests

#### Comment Controller Tests

- **`CommentControllerUnitTest.java`** - Unit tests for CommentController
  - Tests controller methods in isolation using Mockito
  - Tests HTTP response mapping and status codes
  - Tests exception handling and error responses
  - Tests all REST endpoints (GET, POST, DELETE)
  - Tests authentication and authorization scenarios

#### Task Controller Tests

- **`TaskControllerUnitTest.java`** - Unit tests for TaskController
  - Tests controller methods in isolation using Mockito
  - Tests HTTP response mapping and status codes
  - Tests exception handling and error responses
  - Tests all REST endpoints (GET, POST, PUT, PATCH, DELETE)
  - Tests complex operations like user/tag assignment

## Test Coverage

### Comment Management

- ✅ **Repository Layer**: CRUD operations, custom queries, edge cases
- ✅ **Service Layer**: Business logic, authorization, transactions, error handling
- ✅ **Controller Layer**: HTTP endpoints, JSON handling, security, validation

### Task Management

- ✅ **Repository Layer**: CRUD operations, complex queries, pagination, filtering
- ✅ **Service Layer**: Business logic, user/tag assignment, status management, transactions
- ✅ **Controller Layer**: HTTP endpoints, JSON handling, complex operations, validation

## Test Data Patterns

### Test Users

- **Regular Users**: `user1`, `user2` with `USER` role
- **Admin User**: `admin` with `ADMIN` role
- **Different Users**: For testing authorization and assignment scenarios

### Test Projects

- **Project 1**: Owned by user1, contains multiple tasks
- **Project 2**: Owned by user2, for testing cross-project scenarios

### Test Tasks

- **Basic Tasks**: Standard tasks with all fields populated
- **Tasks with Different Statuses**: TODO, IN_PROGRESS, DONE, ARCHIVED
- **Tasks with Different Priorities**: LOW, MEDIUM, HIGH, URGENT
- **Tasks with Assignees**: For testing user assignment functionality
- **Tasks with Tags**: For testing tag assignment functionality

### Test Comments

- **Comments on Different Tasks**: For testing task-specific comment retrieval
- **Comments by Different Users**: For testing authorization scenarios
- **Comments with Various Content**: Empty, null, and normal content

## Running the Tests

### Run All Comment and Task Tests

```bash
mvn test -Dtest="*Comment*Test,*Task*Test"
```

### Run Specific Test Classes

```bash
# Repository tests
mvn test -Dtest=CommentRepositoryUnitTest
mvn test -Dtest=TaskRepositoryUnitTest

# Service integration tests
mvn test -Dtest=CommentServiceIntegrationTest
mvn test -Dtest=TaskServiceIntegrationTest

# Controller unit tests
mvn test -Dtest=CommentControllerUnitTest
mvn test -Dtest=TaskControllerUnitTest
```

### Run Tests by Category

```bash
# All repository tests
mvn test -Dtest="*Repository*Test"

# All service tests
mvn test -Dtest="*Service*Test"

# All controller tests
mvn test -Dtest="*Controller*Test"
```

## Test Scenarios Covered

### Comment Management Scenarios

1. **Basic CRUD Operations**

   - Creating comments on tasks
   - Retrieving comments by task
   - Deleting comments (by author or admin)

2. **Authorization Scenarios**

   - Users can only delete their own comments
   - Admins can delete any comment
   - Unauthorized users cannot delete comments

3. **Edge Cases**
   - Comments with empty/null content
   - Comments on non-existent tasks
   - Comments by non-existent users

### Task Management Scenarios

1. **Basic CRUD Operations**

   - Creating tasks in projects
   - Retrieving tasks by various criteria
   - Updating task properties
   - Deleting tasks

2. **Complex Operations**

   - Assigning users to tasks
   - Assigning tags to tasks
   - Filtering tasks by status, priority, due date
   - Pagination support

3. **Business Logic**

   - Task status transitions
   - Due date management
   - Overdue task detection
   - User-task relationships

4. **Edge Cases**
   - Tasks with minimal data
   - Tasks with null/empty fields
   - Non-existent entity references
   - Invalid user/tag assignments

## Assertions and Validations

### Repository Tests

- Data persistence verification
- Query result accuracy
- Empty result handling
- Exception throwing for invalid operations

### Service Tests

- Business logic correctness
- Authorization enforcement
- Transaction rollback on errors
- Data integrity maintenance
- Complex operation validation

### Controller Tests

- HTTP status code validation
- Response body structure validation
- Error response format validation
- Security constraint enforcement
- JSON serialization/deserialization

## Best Practices Implemented

1. **Test Isolation**: Each test is independent with clean setup
2. **Comprehensive Coverage**: Tests cover happy path, edge cases, and error conditions
3. **Realistic Test Data**: Test data mirrors production scenarios
4. **Clear Assertions**: Descriptive assertions with meaningful error messages
5. **Proper Mocking**: Appropriate use of mocks vs real objects
6. **Transaction Management**: Proper handling of test transactions
7. **Security Testing**: Authentication and authorization validation
8. **Data Validation**: Testing with various data types and edge cases

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
- Use `@DirtiesContext` for tests that modify application context

## Test Statistics

- **Total Test Files**: 6
- **Repository Tests**: 25+ test methods
- **Service Integration Tests**: 30+ test methods
- **Controller Unit Tests**: 35+ test methods
- **Total Test Methods**: ~90 comprehensive test methods

The test suite provides excellent coverage of your comment and task management functionality, ensuring reliability, maintainability, and confidence in your codebase. All tests are properly configured to work with your Spring Boot application and follow industry best practices for testing Spring applications.
