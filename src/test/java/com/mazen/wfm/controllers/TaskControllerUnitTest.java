package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.TaskRequest;
import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.dtos.response.TaskResponse;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.TaskMapper;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Status;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.services.TaskService;
import com.mazen.wfm.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest {

  @Mock
  private TaskService taskService;

  @Mock
  private TaskMapper taskMapper;

  @Mock
  private UserService userService;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private TaskController taskController;

  private AppUser testUser;
  private Project testProject;
  private Task testTask;
  private TaskResponse testTaskResponse;
  private TaskRequest taskRequest;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testUser = AppUser.builder()
        .userId(1L)
        .username("testuser")
        .password("password")
        .fullName("Test User")
        .email("test@example.com")
        .role(UserRole.USER)
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();

    testProject = new Project();
    testProject.setProjectId(1L);
    testProject.setName("Test Project");
    testProject.setDescription("Test Description");
    testProject.setCreatedAt(LocalDateTime.now());
    testProject.setOwner(testUser);

    testTask = new Task();
    testTask.setTaskId(1L);
    testTask.setTitle("Test Task");
    testTask.setDescription("Test Description");
    testTask.setStatus(Status.TODO);
    testTask.setPriority(Priority.MEDIUM);
    testTask.setDueDate(LocalDate.now().plusDays(7));
    testTask.setProject(testProject);
    testTask.setCreatedAt(LocalDateTime.now());
    testTask.setUpdatedAt(LocalDateTime.now());

    testTaskResponse = TaskResponse.builder()
        .taskId(1L)
        .title("Test Task")
        .description("Test Description")
        .status(Status.TODO)
        .priority(Priority.MEDIUM)
        .dueDate(LocalDate.now().plusDays(7))
        .projectId(1L)
        .projectName("Test Project")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    taskRequest = new TaskRequest(
        "Test Task",
        "Test Description",
        Status.TODO,
        Priority.MEDIUM,
        LocalDate.now().plusDays(7),
        1L,
        null,
        Set.of());

    pageable = PageRequest.of(0, 10);

    lenient().when(authentication.getName()).thenReturn("testuser");
  }

  @Test
  void testGetTask_ShouldReturnTaskSuccessfully() {
    // Given
    when(taskService.getTaskById(1L)).thenReturn(testTask);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.getTask(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(testTaskResponse);
    assertThat(response.getBody().getMessage()).isEqualTo("Operation successful");

    verify(taskService).getTaskById(1L);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testGetTask_WithNonExistentId_ShouldThrowException() {
    // Given
    when(taskService.getTaskById(999L))
        .thenThrow(new ResourceNotFoundException("No task with this id"));

    // When & Then
    assertThatThrownBy(() -> taskController.getTask(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");

    verify(taskService).getTaskById(999L);
    verify(taskMapper, never()).toResponse(any());
  }

  @Test
  void testGetTasksByProjectAndStatus_ShouldReturnTasksSuccessfully() {
    // Given
    List<Task> tasks = List.of(testTask);
    List<TaskResponse> taskResponses = List.of(testTaskResponse);

    when(taskService.getTasksByProjectAndStatus(1L, Status.TODO)).thenReturn(tasks);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<List<TaskResponse>>> response = taskController.getTasksByProjectAndStatus(1L,
        Status.TODO);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(taskResponses);

    verify(taskService).getTasksByProjectAndStatus(1L, Status.TODO);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testGetTasksByProjectAndStatus_WithNoTasks_ShouldReturnEmptyList() {
    // Given
    when(taskService.getTasksByProjectAndStatus(1L, Status.DONE)).thenReturn(List.of());

    // When
    ResponseEntity<ResponseWrapper<List<TaskResponse>>> response = taskController.getTasksByProjectAndStatus(1L,
        Status.DONE);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEmpty();

    verify(taskService).getTasksByProjectAndStatus(1L, Status.DONE);
    verify(taskMapper, never()).toResponse(any());
  }

  @Test
  void testGetTasksByProject_ShouldReturnPaginatedTasksSuccessfully() {
    // Given
    Page<Task> taskPage = new PageImpl<>(List.of(testTask), pageable, 1);

    when(taskService.getTasksByProject(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<Page<TaskResponse>>> response = taskController.getTasksByProject(1L, pageable);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData().getContent()).hasSize(1);
    assertThat(response.getBody().getData().getContent().get(0)).isEqualTo(testTaskResponse);

    verify(taskService).getTasksByProject(1L, pageable);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testGetTasksByUser_ShouldReturnUserTasksSuccessfully() {
    // Given
    List<Task> tasks = List.of(testTask);
    List<TaskResponse> taskResponses = List.of(testTaskResponse);

    when(taskService.getTasksByUser(1L)).thenReturn(tasks);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<List<TaskResponse>>> response = taskController.getTasksByUser(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(taskResponses);

    verify(taskService).getTasksByUser(1L);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testCreateTask_ShouldCreateTaskSuccessfully() {
    // Given
    when(taskMapper.toEntity(taskRequest)).thenReturn(testTask);
    when(taskService.createTask(testTask)).thenReturn(testTask);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.createTask(taskRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(testTaskResponse);

    verify(taskMapper).toEntity(taskRequest);
    verify(taskService).createTask(testTask);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testCreateTask_WithInvalidRequest_ShouldThrowException() {
    // Given
    TaskRequest invalidRequest = new TaskRequest(
        "", // Empty title
        "Description",
        Status.TODO,
        Priority.MEDIUM,
        LocalDate.now().plusDays(7),
        1L,
        null,
        Set.of());

    when(taskMapper.toEntity(invalidRequest)).thenReturn(testTask);
    when(taskService.createTask(testTask))
        .thenThrow(new IllegalArgumentException("Task title cannot be empty"));

    // When & Then
    assertThatThrownBy(() -> taskController.createTask(invalidRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Task title cannot be empty");

    verify(taskMapper).toEntity(invalidRequest);
    verify(taskService).createTask(testTask);
  }

  @Test
  void testAssignTagsToTask_ShouldAssignTagsSuccessfully() {
    // Given
    Set<Long> tagIds = Set.of(1L, 2L);
    when(taskService.assignTags(tagIds, 1L)).thenReturn(testTask);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.AssignTagsToTask(1L, tagIds);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(testTaskResponse);

    verify(taskService).assignTags(tagIds, 1L);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testAssignTagsToTask_WithNonExistentTask_ShouldThrowException() {
    // Given
    Set<Long> tagIds = Set.of(1L, 2L);
    when(taskService.assignTags(tagIds, 999L))
        .thenThrow(new ResourceNotFoundException("No task with this id"));

    // When & Then
    assertThatThrownBy(() -> taskController.AssignTagsToTask(999L, tagIds))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");

    verify(taskService).assignTags(tagIds, 999L);
  }

  @Test
  void testAssignUsers_ShouldAssignUsersSuccessfully() {
    // Given
    Set<Long> userIds = Set.of(1L, 2L);
    when(taskService.assignUsersToTask(1L, userIds)).thenReturn(testTask);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.assignUsers(1L, userIds);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(testTaskResponse);

    verify(taskService).assignUsersToTask(1L, userIds);
    verify(taskMapper).toResponse(testTask);
  }

  @Test
  void testAssignUsers_WithNonExistentTask_ShouldThrowException() {
    // Given
    Set<Long> userIds = Set.of(1L, 2L);
    when(taskService.assignUsersToTask(999L, userIds))
        .thenThrow(new ResourceNotFoundException("No task with this id"));

    // When & Then
    assertThatThrownBy(() -> taskController.assignUsers(999L, userIds))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");

    verify(taskService).assignUsersToTask(999L, userIds);
  }

  @Test
  void testAssignUsers_WithNonExistentUsers_ShouldThrowException() {
    // Given
    Set<Long> nonExistentUserIds = Set.of(999L, 998L);
    when(taskService.assignUsersToTask(1L, nonExistentUserIds))
        .thenThrow(new ResourceNotFoundException("No valid users found for given IDs"));

    // When & Then
    assertThatThrownBy(() -> taskController.assignUsers(1L, nonExistentUserIds))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No valid users found for given IDs");

    verify(taskService).assignUsersToTask(1L, nonExistentUserIds);
  }

  @Test
  void testUpdateTask_ShouldUpdateTaskSuccessfully() {
    // Given
    TaskRequest updateRequest = new TaskRequest(
        "Updated Task",
        "Updated Description",
        Status.IN_PROGRESS,
        Priority.HIGH,
        LocalDate.now().plusDays(5),
        1L,
        null,
        Set.of());

    Task updatedTask = new Task();
    updatedTask.setTaskId(1L);
    updatedTask.setTitle("Updated Task");
    updatedTask.setDescription("Updated Description");
    updatedTask.setStatus(Status.IN_PROGRESS);
    updatedTask.setPriority(Priority.HIGH);
    updatedTask.setProject(testProject);
    updatedTask.setCreatedAt(LocalDateTime.now());
    updatedTask.setUpdatedAt(LocalDateTime.now());

    TaskResponse updatedResponse = TaskResponse.builder()
        .taskId(1L)
        .title("Updated Task")
        .description("Updated Description")
        .status(Status.IN_PROGRESS)
        .priority(Priority.HIGH)
        .projectId(1L)
        .projectName("Test Project")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(taskService.updateTask(1L, updateRequest)).thenReturn(updatedTask);
    when(taskMapper.toResponse(updatedTask)).thenReturn(updatedResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.updateTask(1L, updateRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(updatedResponse);

    verify(taskService).updateTask(1L, updateRequest);
    verify(taskMapper).toResponse(updatedTask);
  }

  @Test
  void testUpdateTask_WithNonExistentId_ShouldThrowException() {
    // Given
    TaskRequest updateRequest = new TaskRequest(
        "Updated Task",
        "Updated Description",
        Status.IN_PROGRESS,
        Priority.HIGH,
        LocalDate.now().plusDays(5),
        1L,
        null,
        Set.of());

    when(taskService.updateTask(999L, updateRequest))
        .thenThrow(new ResourceNotFoundException("No task with this id"));

    // When & Then
    assertThatThrownBy(() -> taskController.updateTask(999L, updateRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");

    verify(taskService).updateTask(999L, updateRequest);
  }

  @Test
  void testClearTaskAssignees_ShouldClearAssigneesSuccessfully() {
    // Given
    doNothing().when(taskService).clearAssignees(1L);

    // When
    ResponseEntity<Void> response = taskController.clearTaskAssignees(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(taskService).clearAssignees(1L);
  }

  @Test
  void testClearTaskTags_ShouldClearTagsSuccessfully() {
    // Given
    doNothing().when(taskService).clearTags(1L);

    // When
    ResponseEntity<Void> response = taskController.clearTaskTags(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(taskService).clearTags(1L);
  }

  @Test
  void testDeleteTask_ShouldDeleteTaskSuccessfully() {
    // Given
    doNothing().when(taskService).deleteTask(1L);

    // When
    ResponseEntity<Void> response = taskController.deleteTask(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(taskService).deleteTask(1L);
  }

  @Test
  void testDeleteTask_WithNonExistentId_ShouldThrowException() {
    // Given
    doThrow(new ResourceNotFoundException("No task with this id"))
        .when(taskService).deleteTask(999L);

    // When & Then
    assertThatThrownBy(() -> taskController.deleteTask(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No task with this id");

    verify(taskService).deleteTask(999L);
  }

  @Test
  void testCreateTask_WithMinimalData_ShouldCreateTaskSuccessfully() {
    // Given
    TaskRequest minimalRequest = new TaskRequest(
        "Minimal Task",
        null,
        null,
        null,
        null,
        1L,
        null,
        null);

    Task minimalTask = new Task();
    minimalTask.setTitle("Minimal Task");
    minimalTask.setProject(testProject);
    minimalTask.setCreatedAt(LocalDateTime.now());
    minimalTask.setUpdatedAt(LocalDateTime.now());

    TaskResponse minimalResponse = TaskResponse.builder()
        .taskId(1L)
        .title("Minimal Task")
        .projectId(1L)
        .projectName("Test Project")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(taskMapper.toEntity(minimalRequest)).thenReturn(minimalTask);
    when(taskService.createTask(minimalTask)).thenReturn(minimalTask);
    when(taskMapper.toResponse(minimalTask)).thenReturn(minimalResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.createTask(minimalRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData().getTitle()).isEqualTo("Minimal Task");

    verify(taskMapper).toEntity(minimalRequest);
    verify(taskService).createTask(minimalTask);
    verify(taskMapper).toResponse(minimalTask);
  }

  @Test
  void testAssignTagsToTask_WithEmptyTagSet_ShouldHandleGracefully() {
    // Given
    Set<Long> emptyTagIds = Set.of();
    when(taskService.assignTags(emptyTagIds, 1L)).thenReturn(testTask);
    when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

    // When
    ResponseEntity<ResponseWrapper<TaskResponse>> response = taskController.AssignTagsToTask(1L, emptyTagIds);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();

    verify(taskService).assignTags(emptyTagIds, 1L);
    verify(taskMapper).toResponse(testTask);
  }
}
