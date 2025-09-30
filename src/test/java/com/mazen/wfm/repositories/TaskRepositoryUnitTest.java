package com.mazen.wfm.repositories;

import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Status;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryUnitTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TaskRepository taskRepository;

  private AppUser testUser1;
  private AppUser testUser2;
  private Project testProject1;
  private Project testProject2;
  private Task testTask1;
  private Task testTask2;
  private Task testTask3;
  private Task testTask4;

  @BeforeEach
  void setUp() {
    // Create test users
    testUser1 = AppUser.builder()
        .username("user1")
        .password("password")
        .fullName("User One")
        .email("user1@test.com")
        .role(UserRole.USER)
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();

    testUser2 = AppUser.builder()
        .username("user2")
        .password("password")
        .fullName("User Two")
        .email("user2@test.com")
        .role(UserRole.USER)
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();

    // Persist users
    testUser1 = entityManager.persistAndFlush(testUser1);
    testUser2 = entityManager.persistAndFlush(testUser2);

    // Create test projects
    testProject1 = new Project();
    testProject1.setName("Project 1");
    testProject1.setDescription("Description for project 1");
    testProject1.setCreatedAt(LocalDateTime.now());
    testProject1.setOwner(testUser1);
    testProject1 = entityManager.persistAndFlush(testProject1);

    testProject2 = new Project();
    testProject2.setName("Project 2");
    testProject2.setDescription("Description for project 2");
    testProject2.setCreatedAt(LocalDateTime.now());
    testProject2.setOwner(testUser2);
    testProject2 = entityManager.persistAndFlush(testProject2);

    // Create test tasks
    testTask1 = new Task();
    testTask1.setTitle("Task 1");
    testTask1.setDescription("Description for task 1");
    testTask1.setStatus(Status.TODO);
    testTask1.setPriority(Priority.MEDIUM);
    testTask1.setDueDate(LocalDate.now().plusDays(7));
    testTask1.setProject(testProject1);
    testTask1.setCreatedAt(LocalDateTime.now());
    testTask1.setUpdatedAt(LocalDateTime.now());
    testTask1 = entityManager.persistAndFlush(testTask1);

    testTask2 = new Task();
    testTask2.setTitle("Task 2");
    testTask2.setDescription("Description for task 2");
    testTask2.setStatus(Status.IN_PROGRESS);
    testTask2.setPriority(Priority.HIGH);
    testTask2.setDueDate(LocalDate.now().plusDays(3));
    testTask2.setProject(testProject1);
    testTask2.setCreatedAt(LocalDateTime.now());
    testTask2.setUpdatedAt(LocalDateTime.now());
    testTask2 = entityManager.persistAndFlush(testTask2);

    testTask3 = new Task();
    testTask3.setTitle("Task 3");
    testTask3.setDescription("Description for task 3");
    testTask3.setStatus(Status.DONE);
    testTask3.setPriority(Priority.LOW);
    testTask3.setDueDate(LocalDate.now().minusDays(1));
    testTask3.setProject(testProject2);
    testTask3.setCreatedAt(LocalDateTime.now());
    testTask3.setUpdatedAt(LocalDateTime.now());
    testTask3 = entityManager.persistAndFlush(testTask3);

    testTask4 = new Task();
    testTask4.setTitle("Task 4");
    testTask4.setDescription("Description for task 4");
    testTask4.setStatus(Status.TODO);
    testTask4.setPriority(Priority.URGENT);
    testTask4.setDueDate(LocalDate.now().minusDays(2));
    testTask4.setProject(testProject1);
    testTask4.setCreatedAt(LocalDateTime.now());
    testTask4.setUpdatedAt(LocalDateTime.now());
    testTask4 = entityManager.persistAndFlush(testTask4);

    // Assign users to tasks
    testTask1.getAssignees().add(testUser1);
    testTask2.getAssignees().add(testUser1);
    testTask2.getAssignees().add(testUser2);
    testTask3.getAssignees().add(testUser2);
    testTask4.getAssignees().add(testUser1);

    entityManager.flush();
  }

  @Test
  void testFindByProject_ProjectId_ShouldReturnTasksForSpecificProject() {
    // When
    List<Task> project1Tasks = taskRepository.findByProject_ProjectId(testProject1.getProjectId());
    List<Task> project2Tasks = taskRepository.findByProject_ProjectId(testProject2.getProjectId());

    // Then
    assertThat(project1Tasks).hasSize(3);
    assertThat(project1Tasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 4");
    assertThat(project1Tasks).extracting(Task::getProject)
        .allMatch(project -> project.getProjectId().equals(testProject1.getProjectId()));

    assertThat(project2Tasks).hasSize(1);
    assertThat(project2Tasks).extracting(Task::getTitle)
        .containsExactly("Task 3");
    assertThat(project2Tasks).extracting(Task::getProject)
        .allMatch(project -> project.getProjectId().equals(testProject2.getProjectId()));
  }

  @Test
  void testFindByProject_ProjectId_WithPageable_ShouldReturnPaginatedTasks() {
    // Given
    Pageable pageable = PageRequest.of(0, 2);

    // When
    Page<Task> project1TasksPage = taskRepository.findByProject_ProjectId(testProject1.getProjectId(), pageable);

    // Then
    assertThat(project1TasksPage.getContent()).hasSize(2);
    assertThat(project1TasksPage.getTotalElements()).isEqualTo(3);
    assertThat(project1TasksPage.getTotalPages()).isEqualTo(2);
    assertThat(project1TasksPage.getContent()).extracting(Task::getProject)
        .allMatch(project -> project.getProjectId().equals(testProject1.getProjectId()));
  }

  @Test
  void testFindByProject_ProjectId_ShouldReturnEmptyListForNonExistentProject() {
    // When
    List<Task> nonExistentProjectTasks = taskRepository.findByProject_ProjectId(999L);

    // Then
    assertThat(nonExistentProjectTasks).isEmpty();
  }

  @Test
  void testFindByAssignees_UserId_ShouldReturnTasksAssignedToSpecificUser() {
    // When
    List<Task> user1Tasks = taskRepository.findByAssignees_UserId(testUser1.getUserId());
    List<Task> user2Tasks = taskRepository.findByAssignees_UserId(testUser2.getUserId());

    // Then
    assertThat(user1Tasks).hasSize(3);
    assertThat(user1Tasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 4");
    assertThat(user1Tasks).extracting(Task::getAssignees)
        .allMatch(assignees -> assignees.contains(testUser1));

    assertThat(user2Tasks).hasSize(2);
    assertThat(user2Tasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 2", "Task 3");
    assertThat(user2Tasks).extracting(Task::getAssignees)
        .allMatch(assignees -> assignees.contains(testUser2));
  }

  @Test
  void testFindByAssignees_UserId_ShouldReturnEmptyListForNonExistentUser() {
    // When
    List<Task> nonExistentUserTasks = taskRepository.findByAssignees_UserId(999L);

    // Then
    assertThat(nonExistentUserTasks).isEmpty();
  }

  @Test
  void testFindByStatus_ShouldReturnTasksWithSpecificStatus() {
    // When
    List<Task> todoTasks = taskRepository.findByStatus(Status.TODO);
    List<Task> inProgressTasks = taskRepository.findByStatus(Status.IN_PROGRESS);
    List<Task> doneTasks = taskRepository.findByStatus(Status.DONE);

    // Then
    assertThat(todoTasks).hasSize(2);
    assertThat(todoTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 4");
    assertThat(todoTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.TODO);

    assertThat(inProgressTasks).hasSize(1);
    assertThat(inProgressTasks).extracting(Task::getTitle)
        .containsExactly("Task 2");
    assertThat(inProgressTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.IN_PROGRESS);

    assertThat(doneTasks).hasSize(1);
    assertThat(doneTasks).extracting(Task::getTitle)
        .containsExactly("Task 3");
    assertThat(doneTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.DONE);
  }

  @Test
  void testFindByDueDateBefore_ShouldReturnOverdueTasks() {
    // Given
    LocalDate today = LocalDate.now();

    // When
    List<Task> overdueTasks = taskRepository.findByDueDateBefore(today);

    // Then
    assertThat(overdueTasks).hasSize(2);
    assertThat(overdueTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 3", "Task 4");
    assertThat(overdueTasks).extracting(Task::getDueDate)
        .allMatch(dueDate -> dueDate.isBefore(today));
  }

  @Test
  void testFindByDueDateBetweenAndStatusIn_ShouldReturnTasksInDateRangeWithSpecificStatuses() {
    // Given
    LocalDate startDate = LocalDate.now().minusDays(5);
    LocalDate endDate = LocalDate.now().plusDays(5);
    List<Status> statuses = List.of(Status.TODO, Status.IN_PROGRESS);

    // When
    List<Task> tasksInRange = taskRepository.findByDueDateBetweenAndStatusIn(startDate, endDate, statuses);

    // Then
    assertThat(tasksInRange).hasSize(2);
    assertThat(tasksInRange).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 2", "Task 4");
    assertThat(tasksInRange).extracting(Task::getStatus)
        .allMatch(status -> statuses.contains(status));
    assertThat(tasksInRange).extracting(Task::getDueDate)
        .allMatch(dueDate -> !dueDate.isBefore(startDate) && !dueDate.isAfter(endDate));
  }

  @Test
  void testFindByProject_ProjectIdAndStatus_ShouldReturnTasksForProjectWithSpecificStatus() {
    // When
    List<Task> project1TodoTasks = taskRepository.findByProject_ProjectIdAndStatus(testProject1.getProjectId(),
        Status.TODO);
    List<Task> project1InProgressTasks = taskRepository.findByProject_ProjectIdAndStatus(testProject1.getProjectId(),
        Status.IN_PROGRESS);

    // Then
    assertThat(project1TodoTasks).hasSize(2);
    assertThat(project1TodoTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 4");
    assertThat(project1TodoTasks).extracting(Task::getProject)
        .allMatch(project -> project.getProjectId().equals(testProject1.getProjectId()));
    assertThat(project1TodoTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.TODO);

    assertThat(project1InProgressTasks).hasSize(1);
    assertThat(project1InProgressTasks).extracting(Task::getTitle)
        .containsExactly("Task 2");
  }

  @Test
  void testFindTasksByUserIdAndStatuses_ShouldReturnTasksForUserWithSpecificStatuses() {
    // Given
    List<Status> statuses = List.of(Status.TODO, Status.IN_PROGRESS);

    // When
    List<Task> user1TasksWithStatuses = taskRepository.findTasksByUserIdAndStatuses(testUser1.getUserId(), statuses);

    // Then
    assertThat(user1TasksWithStatuses).hasSize(3);
    assertThat(user1TasksWithStatuses).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 4");
    assertThat(user1TasksWithStatuses).extracting(Task::getStatus)
        .allMatch(status -> statuses.contains(status));
    assertThat(user1TasksWithStatuses).extracting(Task::getAssignees)
        .allMatch(assignees -> assignees.contains(testUser1));
  }

  @Test
  void testSaveTask_ShouldPersistTaskCorrectly() {
    // Given
    Task newTask = new Task();
    newTask.setTitle("New Task");
    newTask.setDescription("New task description");
    newTask.setStatus(Status.TODO);
    newTask.setPriority(Priority.MEDIUM);
    newTask.setDueDate(LocalDate.now().plusDays(10));
    newTask.setProject(testProject1);
    newTask.setCreatedAt(LocalDateTime.now());
    newTask.setUpdatedAt(LocalDateTime.now());

    // When
    Task savedTask = taskRepository.save(newTask);
    entityManager.flush();

    // Then
    assertThat(savedTask.getTaskId()).isNotNull();
    assertThat(savedTask.getTitle()).isEqualTo("New Task");
    assertThat(savedTask.getDescription()).isEqualTo("New task description");
    assertThat(savedTask.getStatus()).isEqualTo(Status.TODO);
    assertThat(savedTask.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(savedTask.getProject().getProjectId()).isEqualTo(testProject1.getProjectId());

    // Verify it's actually persisted
    List<Task> projectTasks = taskRepository.findByProject_ProjectId(testProject1.getProjectId());
    assertThat(projectTasks).hasSize(4); // Original 3 + new task
    assertThat(projectTasks).extracting(Task::getTitle)
        .contains("New Task");
  }

  @Test
  void testDeleteTask_ShouldRemoveTaskFromDatabase() {
    // Given
    Long taskId = testTask1.getTaskId();

    // When
    taskRepository.deleteById(taskId);
    entityManager.flush();

    // Then
    List<Task> projectTasks = taskRepository.findByProject_ProjectId(testProject1.getProjectId());
    assertThat(projectTasks).hasSize(2); // Only testTask2 and testTask4 remain
    assertThat(projectTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 2", "Task 4");
  }

  @Test
  void testFindById_ShouldReturnCorrectTask() {
    // When
    Task foundTask = taskRepository.findById(testTask1.getTaskId()).orElse(null);

    // Then
    assertThat(foundTask).isNotNull();
    assertThat(foundTask).isEqualTo(testTask1);
    assertThat(foundTask.getTitle()).isEqualTo("Task 1");
    assertThat(foundTask.getStatus()).isEqualTo(Status.TODO);
    assertThat(foundTask.getPriority()).isEqualTo(Priority.MEDIUM);
  }

  @Test
  void testFindById_ShouldReturnEmptyForNonExistentId() {
    // When
    Task foundTask = taskRepository.findById(999L).orElse(null);

    // Then
    assertThat(foundTask).isNull();
  }

  @Test
  void testFindAll_ShouldReturnAllTasks() {
    // When
    List<Task> allTasks = taskRepository.findAll();

    // Then
    assertThat(allTasks).hasSize(4);
    assertThat(allTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 3", "Task 4");
  }

  @Test
  void testTaskWithNullDueDate_ShouldBePersisted() {
    // Given
    Task taskWithNullDueDate = new Task();
    taskWithNullDueDate.setTitle("Task with null due date");
    taskWithNullDueDate.setDescription("Description");
    taskWithNullDueDate.setStatus(Status.TODO);
    taskWithNullDueDate.setPriority(Priority.MEDIUM);
    taskWithNullDueDate.setDueDate(null);
    taskWithNullDueDate.setProject(testProject1);
    taskWithNullDueDate.setCreatedAt(LocalDateTime.now());
    taskWithNullDueDate.setUpdatedAt(LocalDateTime.now());

    // When
    Task savedTask = taskRepository.save(taskWithNullDueDate);

    // Then
    assertThat(savedTask.getDueDate()).isNull();
    assertThat(savedTask.getTaskId()).isNotNull();
  }

  @Test
  void testTaskWithNullDescription_ShouldBePersisted() {
    // Given
    Task taskWithNullDescription = new Task();
    taskWithNullDescription.setTitle("Task with null description");
    taskWithNullDescription.setDescription(null);
    taskWithNullDescription.setStatus(Status.TODO);
    taskWithNullDescription.setPriority(Priority.MEDIUM);
    taskWithNullDescription.setProject(testProject1);
    taskWithNullDescription.setCreatedAt(LocalDateTime.now());
    taskWithNullDescription.setUpdatedAt(LocalDateTime.now());

    // When
    Task savedTask = taskRepository.save(taskWithNullDescription);

    // Then
    assertThat(savedTask.getDescription()).isNull();
    assertThat(savedTask.getTaskId()).isNotNull();
  }
}
