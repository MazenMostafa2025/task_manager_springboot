package com.mazen.wfm.services;

import com.mazen.wfm.dtos.request.CreateCommentRequest;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Comment;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.CommentRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import com.mazen.wfm.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {

  @Autowired
  private CommentService commentService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private ProjectRepository projectRepository;

  private AppUser testUser1;
  private AppUser testUser2;
  private AppUser adminUser;
  private Project testProject;
  private Task testTask1;
  private Task testTask2;
  private Comment testComment1;

  @BeforeEach
  void setUp() {
    // Clean up existing data
    commentRepository.deleteAll();
    taskRepository.deleteAll();
    projectRepository.deleteAll();
    appUserRepository.deleteAll();

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

    adminUser = AppUser.builder()
        .username("admin")
        .password("password")
        .fullName("Admin User")
        .email("admin@test.com")
        .role(UserRole.ADMIN)
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();

    // Persist users
    testUser1 = appUserRepository.save(testUser1);
    testUser2 = appUserRepository.save(testUser2);
    adminUser = appUserRepository.save(adminUser);

    // Create test project
    testProject = new Project();
    testProject.setName("Test Project");
    testProject.setDescription("Test Description");
    testProject.setCreatedAt(LocalDateTime.now());
    testProject.setOwner(testUser1);
    testProject = projectRepository.save(testProject);

    // Create test tasks
    testTask1 = new Task();
    testTask1.setTitle("Task 1");
    testTask1.setDescription("Description for task 1");
    testTask1.setProject(testProject);
    testTask1.setCreatedAt(LocalDateTime.now());
    testTask1.setUpdatedAt(LocalDateTime.now());
    testTask1 = taskRepository.save(testTask1);

    testTask2 = new Task();
    testTask2.setTitle("Task 2");
    testTask2.setDescription("Description for task 2");
    testTask2.setProject(testProject);
    testTask2.setCreatedAt(LocalDateTime.now());
    testTask2.setUpdatedAt(LocalDateTime.now());
    testTask2 = taskRepository.save(testTask2);

    // Create test comment
    testComment1 = new Comment();
    testComment1.setTask(testTask1);
    testComment1.setAuthor(testUser1);
    testComment1.setContent("Test comment");
    testComment1.setCreatedAt(LocalDateTime.now());
    testComment1 = commentRepository.save(testComment1);
  }

  @Test
  void testAddComment_ShouldCreateCommentSuccessfully() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(testTask1.getTaskId(), "New comment content");

    // When
    Comment comment = commentService.addComment(request, testUser1.getUsername());

    // Then
    assertThat(comment).isNotNull();
    assertThat(comment.getCommentId()).isNotNull();
    assertThat(comment.getContent()).isEqualTo("New comment content");
    assertThat(comment.getTask().getTaskId()).isEqualTo(testTask1.getTaskId());
    assertThat(comment.getAuthor().getUserId()).isEqualTo(testUser1.getUserId());
    assertThat(comment.getCreatedAt()).isNotNull();

    // Verify comment is persisted in database
    List<Comment> taskComments = commentRepository.findByTask_TaskId(testTask1.getTaskId());
    assertThat(taskComments).hasSize(2); // Original + new comment
    assertThat(taskComments).extracting(Comment::getContent)
        .contains("New comment content");
  }

  @Test
  void testAddComment_WithNonExistentTask_ShouldThrowException() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(999L, "Comment content");

    // When & Then
    assertThatThrownBy(() -> commentService.addComment(request, testUser1.getUsername()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Task not Found");
  }

  @Test
  void testAddComment_WithNonExistentUser_ShouldThrowException() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(testTask1.getTaskId(), "Comment content");

    // When & Then
    assertThatThrownBy(() -> commentService.addComment(request, "nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not Found");
  }

  @Test
  void testGetCommentById_ShouldReturnCommentSuccessfully() {
    // When
    Comment comment = commentService.getCommentById(testComment1.getCommentId());

    // Then
    assertThat(comment).isNotNull();
    assertThat(comment.getCommentId()).isEqualTo(testComment1.getCommentId());
    assertThat(comment.getContent()).isEqualTo("Test comment");
    assertThat(comment.getTask().getTaskId()).isEqualTo(testTask1.getTaskId());
    assertThat(comment.getAuthor().getUserId()).isEqualTo(testUser1.getUserId());
  }

  @Test
  void testGetCommentById_WithNonExistentId_ShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> commentService.getCommentById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Comment not found");
  }

  @Test
  void testGetCommentsByTask_ShouldReturnTaskComments() {
    // Given - Add another comment to task1
    Comment additionalComment = new Comment();
    additionalComment.setTask(testTask1);
    additionalComment.setAuthor(testUser2);
    additionalComment.setContent("Additional comment");
    additionalComment.setCreatedAt(LocalDateTime.now());
    commentRepository.save(additionalComment);

    // When
    List<Comment> taskComments = commentService.getCommentsByTask(testTask1.getTaskId());

    // Then
    assertThat(taskComments).hasSize(2);
    assertThat(taskComments).extracting(Comment::getContent)
        .containsExactlyInAnyOrder("Test comment", "Additional comment");
    assertThat(taskComments).extracting(Comment::getTask)
        .allMatch(task -> task.getTaskId().equals(testTask1.getTaskId()));
  }

  @Test
  void testGetCommentsByTask_WithNoComments_ShouldReturnEmptyList() {
    // When
    List<Comment> taskComments = commentService.getCommentsByTask(testTask2.getTaskId());

    // Then
    assertThat(taskComments).isEmpty();
  }

  @Test
  void testGetCommentsByTask_WithNonExistentTask_ShouldReturnEmptyList() {
    // When
    List<Comment> taskComments = commentService.getCommentsByTask(999L);

    // Then
    assertThat(taskComments).isEmpty();
  }

  @Test
  void testDeleteComment_ByAuthor_ShouldDeleteCommentSuccessfully() {
    // Given
    Long commentId = testComment1.getCommentId();

    // When
    commentService.deleteComment(commentId, testUser1.getUsername());

    // Then
    assertThat(commentRepository.findById(commentId)).isEmpty();
  }

  @Test
  void testDeleteComment_ByAdmin_ShouldDeleteCommentSuccessfully() {
    // Given
    Long commentId = testComment1.getCommentId();

    // When
    commentService.deleteComment(commentId, adminUser.getUsername());

    // Then
    assertThat(commentRepository.findById(commentId)).isEmpty();
  }

  @Test
  void testDeleteComment_ByNonAuthor_ShouldThrowException() {
    // Given
    Long commentId = testComment1.getCommentId();

    // When & Then
    assertThatThrownBy(() -> commentService.deleteComment(commentId, testUser2.getUsername()))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("You are not allowed to delete this comment");
  }

  @Test
  void testDeleteComment_WithNonExistentComment_ShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> commentService.deleteComment(999L, testUser1.getUsername()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Comment not found");
  }

  @Test
  void testDeleteComment_WithNonExistentUser_ShouldThrowException() {
    // Given
    Long commentId = testComment1.getCommentId();

    // When & Then
    assertThatThrownBy(() -> commentService.deleteComment(commentId, "nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void testAddComment_ShouldSetCorrectTimestamps() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(testTask1.getTaskId(), "Timestamp test comment");
    LocalDateTime beforeCreation = LocalDateTime.now();

    // When
    Comment comment = commentService.addComment(request, testUser1.getUsername());
    LocalDateTime afterCreation = LocalDateTime.now();

    // Then
    assertThat(comment.getCreatedAt()).isNotNull();
    assertThat(comment.getCreatedAt()).isAfter(beforeCreation.minusSeconds(1));
    assertThat(comment.getCreatedAt()).isBefore(afterCreation.plusSeconds(1));
  }

  @Test
  void testAddComment_WithEmptyContent_ShouldCreateCommentSuccessfully() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(testTask1.getTaskId(), "");

    // When
    Comment comment = commentService.addComment(request, testUser1.getUsername());

    // Then
    assertThat(comment).isNotNull();
    assertThat(comment.getContent()).isEqualTo("");
    assertThat(comment.getCommentId()).isNotNull();
  }

  @Test
  void testAddComment_WithNullContent_ShouldCreateCommentSuccessfully() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(testTask1.getTaskId(), null);

    // When
    Comment comment = commentService.addComment(request, testUser1.getUsername());

    // Then
    assertThat(comment).isNotNull();
    assertThat(comment.getContent()).isNull();
    assertThat(comment.getCommentId()).isNotNull();
  }

  @Test
  void testGetCommentsByTask_WithMultipleComments_ShouldReturnAllCommentsInOrder() {
    // Given - Add multiple comments
    Comment comment2 = new Comment();
    comment2.setTask(testTask1);
    comment2.setAuthor(testUser2);
    comment2.setContent("Second comment");
    comment2.setCreatedAt(LocalDateTime.now().plusMinutes(1));
    commentRepository.save(comment2);

    Comment comment3 = new Comment();
    comment3.setTask(testTask1);
    comment3.setAuthor(testUser1);
    comment3.setContent("Third comment");
    comment3.setCreatedAt(LocalDateTime.now().plusMinutes(2));
    commentRepository.save(comment3);

    // When
    List<Comment> taskComments = commentService.getCommentsByTask(testTask1.getTaskId());

    // Then
    assertThat(taskComments).hasSize(3);
    assertThat(taskComments).extracting(Comment::getContent)
        .containsExactlyInAnyOrder("Test comment", "Second comment", "Third comment");
  }

  @Test
  void testAddComment_WithDifferentUsers_ShouldCreateCommentsSuccessfully() {
    // Given
    CreateCommentRequest request1 = new CreateCommentRequest(testTask1.getTaskId(), "Comment from user1");
    CreateCommentRequest request2 = new CreateCommentRequest(testTask1.getTaskId(), "Comment from user2");

    // When
    Comment comment1 = commentService.addComment(request1, testUser1.getUsername());
    Comment comment2 = commentService.addComment(request2, testUser2.getUsername());

    // Then
    assertThat(comment1.getAuthor().getUserId()).isEqualTo(testUser1.getUserId());
    assertThat(comment1.getContent()).isEqualTo("Comment from user1");
    assertThat(comment2.getAuthor().getUserId()).isEqualTo(testUser2.getUserId());
    assertThat(comment2.getContent()).isEqualTo("Comment from user2");

    // Verify both comments are persisted
    List<Comment> taskComments = commentRepository.findByTask_TaskId(testTask1.getTaskId());
    assertThat(taskComments).hasSize(3); // Original + 2 new comments
  }
}
