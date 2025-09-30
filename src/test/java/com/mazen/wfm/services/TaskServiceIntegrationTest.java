package com.mazen.wfm.services;

import com.mazen.wfm.dtos.request.TaskRequest;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Status;
import com.mazen.wfm.models.Tag;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import com.mazen.wfm.repositories.TagRepository;
import com.mazen.wfm.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private TagRepository tagRepository;

  private AppUser testUser1;
  private AppUser testUser2;
  private Project testProject1;
  private Project testProject2;
  private Task testTask1;
  private Task testTask2;
  private Tag testTag1;
  private Tag testTag2;

  @BeforeEach
  void setUp() {
    // Clean up existing data
    taskRepository.deleteAll();
    projectRepository.deleteAll();
    appUserRepository.deleteAll();
    tagRepository.deleteAll();

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
    testUser1 = appUserRepository.save(testUser1);
    testUser2 = appUserRepository.save(testUser2);

    // Create test projects
    testProject1 = new Project();
    testProject1.setName("Project 1");
    testProject1.setDescription("Description for project 1");
    testProject1.setCreatedAt(LocalDateTime.now());
    testProject1.setOwner(testUser1);
    testProject1 = projectRepository.save(testProject1);

    testProject2 = new Project();
    testProject2.setName("Project 2");
    testProject2.setDescription("Description for project 2");
    testProject2.setCreatedAt(LocalDateTime.now());
    testProject2.setOwner(testUser2);
    testProject2 = projectRepository.save(testProject2);

    // Create test tags
    testTag1 = new Tag();
    testTag1.setName("Frontend");
    testTag1 = tagRepository.save(testTag1);

    testTag2 = new Tag();
    testTag2.setName("Backend");
    testTag2 = tagRepository.save(testTag2);

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
    testTask1 = taskRepository.save(testTask1);

    testTask2 = new Task();
    testTask2.setTitle("Task 2");
    testTask2.setDescription("Description for task 2");
    testTask2.setStatus(Status.IN_PROGRESS);
    testTask2.setPriority(Priority.HIGH);
    testTask2.setDueDate(LocalDate.now().plusDays(3));
    testTask2.setProject(testProject1);
    testTask2.setCreatedAt(LocalDateTime.now());
    testTask2.setUpdatedAt(LocalDateTime.now());
    testTask2 = taskRepository.save(testTask2);
  }

  @Test
  void testCreateTask_ShouldCreateTaskSuccessfully() {
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
    Task createdTask = taskService.createTask(newTask);

    // Then
    assertThat(createdTask).isNotNull();
    assertThat(createdTask.getTaskId()).isNotNull();
    assertThat(createdTask.getTitle()).isEqualTo("New Task");
    assertThat(createdTask.getDescription()).isEqualTo("New task description");
    assertThat(createdTask.getStatus()).isEqualTo(Status.TODO);
    assertThat(createdTask.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(createdTask.getProject().getProjectId()).isEqualTo(testProject1.getProjectId());

    // Verify task is persisted in database
    List<Task> projectTasks = taskRepository.findByProject_ProjectId(testProject1.getProjectId());
    assertThat(projectTasks).hasSize(3); // Original 2 + new task
    assertThat(projectTasks).extracting(Task::getTitle)
        .contains("New Task");
  }

  @Test
  void testCreateTask_WithNonExistentProject_ShouldThrowException() {
    // Given
    Task newTask = new Task();
    newTask.setTitle("New Task");
    newTask.setDescription("New task description");
    newTask.setProject(new Project());
    newTask.getProject().setProjectId(999L);

    // When & Then
    assertThatThrownBy(() -> taskService.createTask(newTask))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Project Not Found");
  }

  @Test
  void testGetTaskById_ShouldReturnTaskSuccessfully() {
    // When
    Task task = taskService.getTaskById(testTask1.getTaskId());

    // Then
    assertThat(task).isNotNull();
    assertThat(task.getTaskId()).isEqualTo(testTask1.getTaskId());
    assertThat(task.getTitle()).isEqualTo("Task 1");
    assertThat(task.getDescription()).isEqualTo("Description for task 1");
    assertThat(task.getStatus()).isEqualTo(Status.TODO);
    assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
  }

  @Test
  void testGetTaskById_WithNonExistentId_ShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> taskService.getTaskById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");
  }

  @Test
  void testGetTasksByProject_ShouldReturnProjectTasks() {
    // When
    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> projectTasks = taskService.getTasksByProject(testProject1.getProjectId(), pageable);

    // Then
    assertThat(projectTasks.getContent()).hasSize(2);
    assertThat(projectTasks.getContent()).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2");
    assertThat(projectTasks.getContent()).extracting(Task::getProject)
        .allMatch(project -> project.getProjectId().equals(testProject1.getProjectId()));
  }

  @Test
  void testGetTasksByProject_WithPagination_ShouldReturnPaginatedTasks() {
    // Given
    Pageable pageable = PageRequest.of(0, 1);

    // When
    Page<Task> projectTasks = taskService.getTasksByProject(testProject1.getProjectId(), pageable);

    // Then
    assertThat(projectTasks.getContent()).hasSize(1);
    assertThat(projectTasks.getTotalElements()).isEqualTo(2);
    assertThat(projectTasks.getTotalPages()).isEqualTo(2);
  }

  @Test
  void testGetTasksByProjectAndStatus_ShouldReturnTasksWithSpecificStatus() {
    // When
    List<Task> todoTasks = taskService.getTasksByProjectAndStatus(testProject1.getProjectId(), Status.TODO);
    List<Task> inProgressTasks = taskService.getTasksByProjectAndStatus(testProject1.getProjectId(),
        Status.IN_PROGRESS);

    // Then
    assertThat(todoTasks).hasSize(1);
    assertThat(todoTasks.get(0).getTitle()).isEqualTo("Task 1");
    assertThat(todoTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.TODO);

    assertThat(inProgressTasks).hasSize(1);
    assertThat(inProgressTasks.get(0).getTitle()).isEqualTo("Task 2");
    assertThat(inProgressTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.IN_PROGRESS);
  }

  @Test
  void testGetTasksByUser_ShouldReturnUserTasks() {
    // Given - Assign users to tasks
    testTask1.getAssignees().add(testUser1);
    testTask2.getAssignees().add(testUser1);
    testTask2.getAssignees().add(testUser2);
    taskRepository.save(testTask1);
    taskRepository.save(testTask2);

    // When
    List<Task> user1Tasks = taskService.getTasksByUser(testUser1.getUserId());
    List<Task> user2Tasks = taskService.getTasksByUser(testUser2.getUserId());

    // Then
    assertThat(user1Tasks).hasSize(2);
    assertThat(user1Tasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2");

    assertThat(user2Tasks).hasSize(1);
    assertThat(user2Tasks).extracting(Task::getTitle)
        .containsExactly("Task 2");
  }

  @Test
  void testGetTasksByUserAndStatus_ShouldReturnUserTasksWithSpecificStatuses() {
    // Given
    testTask1.getAssignees().add(testUser1);
    testTask2.getAssignees().add(testUser1);
    taskRepository.save(testTask1);
    taskRepository.save(testTask2);

    List<Status> statuses = List.of(Status.TODO, Status.IN_PROGRESS);

    // When
    List<Task> userTasks = taskService.getTasksByUserAndStatus(testUser1.getUserId(), statuses);

    // Then
    assertThat(userTasks).hasSize(2);
    assertThat(userTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2");
    assertThat(userTasks).extracting(Task::getStatus)
        .allMatch(status -> statuses.contains(status));
  }

  @Test
  void testGetTasksByStatus_ShouldReturnTasksWithSpecificStatus() {
    // When
    List<Task> todoTasks = taskService.getTasksByStatus(Status.TODO);
    List<Task> inProgressTasks = taskService.getTasksByStatus(Status.IN_PROGRESS);

    // Then
    assertThat(todoTasks).hasSize(1);
    assertThat(todoTasks.get(0).getTitle()).isEqualTo("Task 1");
    assertThat(todoTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.TODO);

    assertThat(inProgressTasks).hasSize(1);
    assertThat(inProgressTasks.get(0).getTitle()).isEqualTo("Task 2");
    assertThat(inProgressTasks).extracting(Task::getStatus)
        .allMatch(status -> status == Status.IN_PROGRESS);
  }

  @Test
  void testGetOverdueTasks_ShouldReturnOverdueTasks() {
    // Given
    LocalDate today = LocalDate.now();

    // When
    List<Task> overdueTasks = taskService.getOverdueTasks(today);

    // Then
    assertThat(overdueTasks).isEmpty(); // No overdue tasks in test data

    // Create an overdue task
    Task overdueTask = new Task();
    overdueTask.setTitle("Overdue Task");
    overdueTask.setDescription("Overdue task description");
    overdueTask.setStatus(Status.TODO);
    overdueTask.setPriority(Priority.HIGH);
    overdueTask.setDueDate(LocalDate.now().minusDays(1));
    overdueTask.setProject(testProject1);
    overdueTask.setCreatedAt(LocalDateTime.now());
    overdueTask.setUpdatedAt(LocalDateTime.now());
    taskRepository.save(overdueTask);

    // When
    List<Task> overdueTasksAfter = taskService.getOverdueTasks(today);

    // Then
    assertThat(overdueTasksAfter).hasSize(1);
    assertThat(overdueTasksAfter.get(0).getTitle()).isEqualTo("Overdue Task");
  }

  @Test
  void testUpdateTask_ShouldUpdateTaskSuccessfully() {
    // Given
    TaskRequest request = new TaskRequest(
        "Updated Task Title",
        "Updated description",
        Status.IN_PROGRESS,
        Priority.HIGH,
        LocalDate.now().plusDays(5),
        testProject1.getProjectId(),
        null,
        Set.of());

    // When
    Task updatedTask = taskService.updateTask(testTask1.getTaskId(), request);

    // Then
    assertThat(updatedTask.getTitle()).isEqualTo("Updated Task Title");
    assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
    assertThat(updatedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
    assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(updatedTask.getUpdatedAt()).isNotNull();

    // Verify changes are persisted
    Task persistedTask = taskRepository.findById(testTask1.getTaskId()).orElseThrow();
    assertThat(persistedTask.getTitle()).isEqualTo("Updated Task Title");
    assertThat(persistedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
  }

  @Test
  void testUpdateTask_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
    // Given
    TaskRequest request = new TaskRequest(
        "Only Title Updated",
        null,
        null,
        null,
        null,
        null,
        null,
        null);

    // When
    Task updatedTask = taskService.updateTask(testTask1.getTaskId(), request);

    // Then
    assertThat(updatedTask.getTitle()).isEqualTo("Only Title Updated");
    assertThat(updatedTask.getDescription()).isEqualTo("Description for task 1"); // Original preserved
    assertThat(updatedTask.getStatus()).isEqualTo(Status.TODO); // Original preserved
    assertThat(updatedTask.getPriority()).isEqualTo(Priority.MEDIUM); // Original preserved
  }

  @Test
  void testUpdateTask_WithNonExistentId_ShouldThrowException() {
    // Given
    TaskRequest request = new TaskRequest(
        "Updated Title",
        "Updated description",
        Status.IN_PROGRESS,
        Priority.HIGH,
        LocalDate.now().plusDays(5),
        testProject1.getProjectId(),
        null,
        Set.of());

    // When & Then
    assertThatThrownBy(() -> taskService.updateTask(999L, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");
  }

  @Test
  void testDeleteTask_ShouldDeleteTaskSuccessfully() {
    // Given
    Long taskId = testTask1.getTaskId();

    // When
    taskService.deleteTask(taskId);

    // Then
    assertThat(taskRepository.findById(taskId)).isEmpty();
  }

  @Test
  void testAssignUsersToTask_ShouldAssignUsersSuccessfully() {
    // Given
    Set<Long> userIds = Set.of(testUser1.getUserId(), testUser2.getUserId());

    // When
    Task task = taskService.assignUsersToTask(testTask1.getTaskId(), userIds);

    // Then
    assertThat(task.getAssignees()).hasSize(2);
    assertThat(task.getAssignees()).extracting(AppUser::getUserId)
        .containsExactlyInAnyOrder(testUser1.getUserId(), testUser2.getUserId());

    // Verify assignment is persisted
    Task persistedTask = taskRepository.findById(testTask1.getTaskId()).orElseThrow();
    assertThat(persistedTask.getAssignees()).hasSize(2);
  }

  @Test
  void testAssignUsersToTask_WithNonExistentUsers_ShouldThrowException() {
    // Given
    Set<Long> nonExistentUserIds = Set.of(999L, 998L);

    // When & Then
    assertThatThrownBy(() -> taskService.assignUsersToTask(testTask1.getTaskId(), nonExistentUserIds))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No valid users found for given IDs");
  }

  @Test
  void testAssignUsersToTask_WithNonExistentTask_ShouldThrowException() {
    // Given
    Set<Long> userIds = Set.of(testUser1.getUserId());

    // When & Then
    assertThatThrownBy(() -> taskService.assignUsersToTask(999L, userIds))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");
  }

  @Test
  void testAssignTags_ShouldAssignTagsSuccessfully() {
    // Given
    Set<Long> tagIds = Set.of(testTag1.getTagId(), testTag2.getTagId());

    // When
    Task task = taskService.assignTags(tagIds, testTask1.getTaskId());

    // Then
    assertThat(task.getTags()).hasSize(2);
    assertThat(task.getTags()).extracting(Tag::getTagId)
        .containsExactlyInAnyOrder(testTag1.getTagId(), testTag2.getTagId());

    // Verify assignment is persisted
    Task persistedTask = taskRepository.findById(testTask1.getTaskId()).orElseThrow();
    assertThat(persistedTask.getTags()).hasSize(2);
  }

  @Test
  void testAssignTags_WithNonExistentTags_ShouldThrowException() {
    // Given
    Set<Long> nonExistentTagIds = Set.of(999L, 998L);

    // When & Then
    assertThatThrownBy(() -> taskService.assignTags(nonExistentTagIds, testTask1.getTaskId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No valid tags found for given IDs");
  }

  @Test
  void testClearTags_ShouldRemoveAllTagsFromTask() {
    // Given - Assign tags first
    Set<Long> tagIds = Set.of(testTag1.getTagId(), testTag2.getTagId());
    taskService.assignTags(tagIds, testTask1.getTaskId());

    // When
    taskService.clearTags(testTask1.getTaskId());

    // Then
    Task task = taskRepository.findById(testTask1.getTaskId()).orElseThrow();
    assertThat(task.getTags()).isEmpty();
  }

  @Test
  void testClearAssignees_ShouldRemoveAllAssigneesFromTask() {
    // Given - Assign users first
    Set<Long> userIds = Set.of(testUser1.getUserId(), testUser2.getUserId());
    taskService.assignUsersToTask(testTask1.getTaskId(), userIds);

    // When
    taskService.clearAssignees(testTask1.getTaskId());

    // Then
    Task task = taskRepository.findById(testTask1.getTaskId()).orElseThrow();
    assertThat(task.getAssignees()).isEmpty();
  }

  @Test
  void testSendReminders_ShouldProcessTasksInDateRange() {
    // Given
    LocalDate now = LocalDate.now();
    LocalDate upcoming = LocalDate.now().plusDays(3);

    // Create a task in the reminder range
    Task reminderTask = new Task();
    reminderTask.setTitle("Reminder Task");
    reminderTask.setDescription("Task for reminder");
    reminderTask.setStatus(Status.TODO);
    reminderTask.setPriority(Priority.MEDIUM);
    reminderTask.setDueDate(LocalDate.now().plusDays(2));
    reminderTask.setProject(testProject1);
    reminderTask.setCreatedAt(LocalDateTime.now());
    reminderTask.setUpdatedAt(LocalDateTime.now());
    reminderTask.getAssignees().add(testUser1);
    taskRepository.save(reminderTask);

    // When
    taskService.sendReminders(now, upcoming);

    // Then - This test mainly verifies the method doesn't throw exceptions
    // In a real implementation, you would verify email sending or other side
    // effects
    assertThat(true).isTrue(); // Placeholder assertion
  }
}
