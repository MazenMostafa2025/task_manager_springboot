package com.mazen.wfm.repositories;

import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Comment;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryUnitTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private CommentRepository commentRepository;

  private AppUser testUser1;
  private AppUser testUser2;
  private Project testProject;
  private Task testTask1;
  private Task testTask2;
  private Comment testComment1;
  private Comment testComment2;
  private Comment testComment3;

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

    // Create test project
    testProject = new Project();
    testProject.setName("Test Project");
    testProject.setDescription("Test Description");
    testProject.setCreatedAt(LocalDateTime.now());
    testProject.setOwner(testUser1);
    testProject = entityManager.persistAndFlush(testProject);

    // Create test tasks
    testTask1 = new Task();
    testTask1.setTitle("Task 1");
    testTask1.setDescription("Description for task 1");
    testTask1.setProject(testProject);
    testTask1.setCreatedAt(LocalDateTime.now());
    testTask1.setUpdatedAt(LocalDateTime.now());
    testTask1 = entityManager.persistAndFlush(testTask1);

    testTask2 = new Task();
    testTask2.setTitle("Task 2");
    testTask2.setDescription("Description for task 2");
    testTask2.setProject(testProject);
    testTask2.setCreatedAt(LocalDateTime.now());
    testTask2.setUpdatedAt(LocalDateTime.now());
    testTask2 = entityManager.persistAndFlush(testTask2);

    // Create test comments
    testComment1 = new Comment();
    testComment1.setTask(testTask1);
    testComment1.setAuthor(testUser1);
    testComment1.setContent("First comment on task 1");
    testComment1.setCreatedAt(LocalDateTime.now());

    testComment2 = new Comment();
    testComment2.setTask(testTask1);
    testComment2.setAuthor(testUser2);
    testComment2.setContent("Second comment on task 1");
    testComment2.setCreatedAt(LocalDateTime.now());

    testComment3 = new Comment();
    testComment3.setTask(testTask2);
    testComment3.setAuthor(testUser1);
    testComment3.setContent("Comment on task 2");
    testComment3.setCreatedAt(LocalDateTime.now());

    // Persist comments
    testComment1 = entityManager.persistAndFlush(testComment1);
    testComment2 = entityManager.persistAndFlush(testComment2);
    testComment3 = entityManager.persistAndFlush(testComment3);
  }

  @Test
  void testFindByTask_TaskId_ShouldReturnCommentsForSpecificTask() {
    // When
    List<Comment> task1Comments = commentRepository.findByTask_TaskId(testTask1.getTaskId());
    List<Comment> task2Comments = commentRepository.findByTask_TaskId(testTask2.getTaskId());

    // Then
    assertThat(task1Comments).hasSize(2);
    assertThat(task1Comments).extracting(Comment::getContent)
        .containsExactlyInAnyOrder("First comment on task 1", "Second comment on task 1");
    assertThat(task1Comments).extracting(Comment::getTask)
        .allMatch(task -> task.getTaskId().equals(testTask1.getTaskId()));

    assertThat(task2Comments).hasSize(1);
    assertThat(task2Comments).extracting(Comment::getContent)
        .containsExactly("Comment on task 2");
    assertThat(task2Comments).extracting(Comment::getTask)
        .allMatch(task -> task.getTaskId().equals(testTask2.getTaskId()));
  }

  @Test
  void testFindByTask_TaskId_ShouldReturnEmptyListForNonExistentTask() {
    // When
    List<Comment> nonExistentTaskComments = commentRepository.findByTask_TaskId(999L);

    // Then
    assertThat(nonExistentTaskComments).isEmpty();
  }

  @Test
  void testFindByAuthor_UserId_ShouldReturnCommentsBySpecificUser() {
    // When
    List<Comment> user1Comments = commentRepository.findByAuthor_UserId(testUser1.getUserId());
    List<Comment> user2Comments = commentRepository.findByAuthor_UserId(testUser2.getUserId());

    // Then
    assertThat(user1Comments).hasSize(2);
    assertThat(user1Comments).extracting(Comment::getContent)
        .containsExactlyInAnyOrder("First comment on task 1", "Comment on task 2");
    assertThat(user1Comments).extracting(Comment::getAuthor)
        .allMatch(author -> author.getUserId().equals(testUser1.getUserId()));

    assertThat(user2Comments).hasSize(1);
    assertThat(user2Comments).extracting(Comment::getContent)
        .containsExactly("Second comment on task 1");
    assertThat(user2Comments).extracting(Comment::getAuthor)
        .allMatch(author -> author.getUserId().equals(testUser2.getUserId()));
  }

  @Test
  void testFindByAuthor_UserId_ShouldReturnEmptyListForNonExistentUser() {
    // When
    List<Comment> nonExistentUserComments = commentRepository.findByAuthor_UserId(999L);

    // Then
    assertThat(nonExistentUserComments).isEmpty();
  }

  @Test
  void testSaveComment_ShouldPersistCommentCorrectly() {
    // Given
    Comment newComment = new Comment();
    newComment.setTask(testTask1);
    newComment.setAuthor(testUser1);
    newComment.setContent("New comment");
    newComment.setCreatedAt(LocalDateTime.now());

    // When
    Comment savedComment = commentRepository.save(newComment);
    entityManager.flush();

    // Then
    assertThat(savedComment.getCommentId()).isNotNull();
    assertThat(savedComment.getContent()).isEqualTo("New comment");
    assertThat(savedComment.getTask().getTaskId()).isEqualTo(testTask1.getTaskId());
    assertThat(savedComment.getAuthor().getUserId()).isEqualTo(testUser1.getUserId());

    // Verify it's actually persisted
    List<Comment> taskComments = commentRepository.findByTask_TaskId(testTask1.getTaskId());
    assertThat(taskComments).hasSize(3); // Original 2 + new comment
    assertThat(taskComments).extracting(Comment::getContent)
        .contains("New comment");
  }

  @Test
  void testDeleteComment_ShouldRemoveCommentFromDatabase() {
    // Given
    Long commentId = testComment1.getCommentId();

    // When
    commentRepository.deleteById(commentId);
    entityManager.flush();

    // Then
    List<Comment> taskComments = commentRepository.findByTask_TaskId(testTask1.getTaskId());
    assertThat(taskComments).hasSize(1); // Only testComment2 remains
    assertThat(taskComments).extracting(Comment::getContent)
        .containsExactly("Second comment on task 1");
  }

  @Test
  void testFindById_ShouldReturnCorrectComment() {
    // When
    Comment foundComment = commentRepository.findById(testComment1.getCommentId()).orElse(null);

    // Then
    assertThat(foundComment).isNotNull();
    assertThat(foundComment).isEqualTo(testComment1);
    assertThat(foundComment.getContent()).isEqualTo("First comment on task 1");
    assertThat(foundComment.getAuthor().getUserId()).isEqualTo(testUser1.getUserId());
    assertThat(foundComment.getTask().getTaskId()).isEqualTo(testTask1.getTaskId());
  }

  @Test
  void testFindById_ShouldReturnEmptyForNonExistentId() {
    // When
    Comment foundComment = commentRepository.findById(999L).orElse(null);

    // Then
    assertThat(foundComment).isNull();
  }

  @Test
  void testFindAll_ShouldReturnAllComments() {
    // When
    List<Comment> allComments = commentRepository.findAll();

    // Then
    assertThat(allComments).hasSize(3);
    assertThat(allComments).extracting(Comment::getContent)
        .containsExactlyInAnyOrder(
            "First comment on task 1",
            "Second comment on task 1",
            "Comment on task 2");
  }

  @Test
  void testCommentCreation_ShouldSetCorrectTimestamps() {
    // Given
    LocalDateTime beforeCreation = LocalDateTime.now();
    Comment newComment = new Comment();
    newComment.setTask(testTask1);
    newComment.setAuthor(testUser1);
    newComment.setContent("Timestamp test comment");

    // When
    Comment savedComment = commentRepository.save(newComment);
    LocalDateTime afterCreation = LocalDateTime.now();

    // Then
    assertThat(savedComment.getCreatedAt()).isNotNull();
    assertThat(savedComment.getCreatedAt()).isAfter(beforeCreation.minusSeconds(1));
    assertThat(savedComment.getCreatedAt()).isBefore(afterCreation.plusSeconds(1));
  }

  @Test
  void testCommentWithEmptyContent_ShouldBePersisted() {
    // Given
    Comment emptyComment = new Comment();
    emptyComment.setTask(testTask1);
    emptyComment.setAuthor(testUser1);
    emptyComment.setContent("");

    // When
    Comment savedComment = commentRepository.save(emptyComment);

    // Then
    assertThat(savedComment.getContent()).isEqualTo("");
    assertThat(savedComment.getCommentId()).isNotNull();
  }

  @Test
  void testCommentWithNullContent_ShouldBePersisted() {
    // Given
    Comment nullComment = new Comment();
    nullComment.setTask(testTask1);
    nullComment.setAuthor(testUser1);
    nullComment.setContent(null);

    // When
    Comment savedComment = commentRepository.save(nullComment);

    // Then
    assertThat(savedComment.getContent()).isNull();
    assertThat(savedComment.getCommentId()).isNotNull();
  }
}
