package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.CreateCommentRequest;
import com.mazen.wfm.dtos.response.CommentResponse;
import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.CommentMapper;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Comment;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerUnitTest {

  @Mock
  private CommentService commentService;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private CommentController commentController;

  private AppUser testUser;
  private Project testProject;
  private Task testTask;
  private Comment testComment;
  private CommentResponse testCommentResponse;
  private CreateCommentRequest createRequest;

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
    testTask.setProject(testProject);
    testTask.setCreatedAt(LocalDateTime.now());
    testTask.setUpdatedAt(LocalDateTime.now());

    testComment = new Comment();
    testComment.setCommentId(1L);
    testComment.setTask(testTask);
    testComment.setAuthor(testUser);
    testComment.setContent("Test comment content");
    testComment.setCreatedAt(LocalDateTime.now());

    testCommentResponse = new CommentResponse(
        1L,
        1L,
        1L,
        "testuser",
        "Test comment content",
        LocalDateTime.now());

    createRequest = new CreateCommentRequest(1L, "New comment content");

    lenient().when(authentication.getName()).thenReturn("testuser");
  }

  @Test
  void testGetTaskComments_ShouldReturnCommentsSuccessfully() {
    // Given
    List<Comment> comments = List.of(testComment);
    List<CommentResponse> commentResponses = List.of(testCommentResponse);

    when(commentService.getCommentsByTask(1L)).thenReturn(comments);
    when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);

    // When
    ResponseEntity<ResponseWrapper<List<CommentResponse>>> response = commentController.getTaskComments(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(commentResponses);
    assertThat(response.getBody().getMessage()).isEqualTo("Operation successful");

    verify(commentService).getCommentsByTask(1L);
    verify(commentMapper).toResponse(testComment);
  }

  @Test
  void testGetTaskComments_WithNoComments_ShouldReturnEmptyList() {
    // Given
    when(commentService.getCommentsByTask(1L)).thenReturn(List.of());

    // When
    ResponseEntity<ResponseWrapper<List<CommentResponse>>> response = commentController.getTaskComments(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEmpty();

    verify(commentService).getCommentsByTask(1L);
    verify(commentMapper, never()).toResponse(any());
  }

  @Test
  void testGetTaskComments_WithMultipleComments_ShouldReturnAllComments() {
    // Given
    Comment comment2 = new Comment();
    comment2.setCommentId(2L);
    comment2.setTask(testTask);
    comment2.setAuthor(testUser);
    comment2.setContent("Second comment");
    comment2.setCreatedAt(LocalDateTime.now());

    CommentResponse response2 = new CommentResponse(
        2L,
        1L,
        1L,
        "testuser",
        "Second comment",
        LocalDateTime.now());

    List<Comment> comments = List.of(testComment, comment2);
    List<CommentResponse> commentResponses = List.of(testCommentResponse, response2);

    when(commentService.getCommentsByTask(1L)).thenReturn(comments);
    when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);
    when(commentMapper.toResponse(comment2)).thenReturn(response2);

    // When
    ResponseEntity<ResponseWrapper<List<CommentResponse>>> response = commentController.getTaskComments(1L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).hasSize(2);
    assertThat(response.getBody().getData()).isEqualTo(commentResponses);

    verify(commentService).getCommentsByTask(1L);
    verify(commentMapper).toResponse(testComment);
    verify(commentMapper).toResponse(comment2);
  }

  @Test
  void testAddComment_ShouldCreateCommentSuccessfully() {
    // Given
    when(commentService.addComment(createRequest, "testuser")).thenReturn(testComment);
    when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);

    // When
    ResponseEntity<ResponseWrapper<CommentResponse>> response = commentController.addComment(createRequest,
        authentication);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(testCommentResponse);
    assertThat(response.getBody().getMessage()).isEqualTo("Operation successful");

    verify(commentService).addComment(createRequest, "testuser");
    verify(commentMapper).toResponse(testComment);
  }

  @Test
  void testAddComment_WithInvalidRequest_ShouldThrowException() {
    // Given
    CreateCommentRequest invalidRequest = new CreateCommentRequest(null, "");
    when(commentService.addComment(invalidRequest, "testuser"))
        .thenThrow(new IllegalArgumentException("Task ID cannot be null"));

    // When & Then
    assertThatThrownBy(() -> commentController.addComment(invalidRequest, authentication))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Task ID cannot be null");

    verify(commentService).addComment(invalidRequest, "testuser");
  }

  @Test
  void testAddComment_WithNonExistentTask_ShouldThrowException() {
    // Given
    CreateCommentRequest request = new CreateCommentRequest(999L, "Comment content");
    when(commentService.addComment(request, "testuser"))
        .thenThrow(new ResourceNotFoundException("Task not Found"));

    // When & Then
    assertThatThrownBy(() -> commentController.addComment(request, authentication))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Task not Found");

    verify(commentService).addComment(request, "testuser");
  }

  @Test
  void testAddComment_WithNonExistentUser_ShouldThrowException() {
    // Given
    when(commentService.addComment(createRequest, "testuser"))
        .thenThrow(new ResourceNotFoundException("User not Found"));

    // When & Then
    assertThatThrownBy(() -> commentController.addComment(createRequest, authentication))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not Found");

    verify(commentService).addComment(createRequest, "testuser");
  }

  @Test
  void testDeleteComment_ShouldDeleteCommentSuccessfully() {
    // Given
    doNothing().when(commentService).deleteComment(1L, "testuser");

    // When
    ResponseEntity<Void> response = commentController.deleteComment(1L, authentication);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(commentService).deleteComment(1L, "testuser");
  }

  @Test
  void testDeleteComment_WithNonExistentComment_ShouldThrowException() {
    // Given
    doThrow(new ResourceNotFoundException("Comment not found"))
        .when(commentService).deleteComment(999L, "testuser");

    // When & Then
    assertThatThrownBy(() -> commentController.deleteComment(999L, authentication))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Comment not found");

    verify(commentService).deleteComment(999L, "testuser");
  }

  @Test
  void testDeleteComment_WithUnauthorizedUser_ShouldThrowException() {
    // Given
    doThrow(new org.springframework.security.access.AccessDeniedException("You are not allowed to delete this comment"))
        .when(commentService).deleteComment(1L, "testuser");

    // When & Then
    assertThatThrownBy(() -> commentController.deleteComment(1L, authentication))
        .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
        .hasMessage("You are not allowed to delete this comment");

    verify(commentService).deleteComment(1L, "testuser");
  }

  @Test
  void testDeleteComment_WithNonExistentUser_ShouldThrowException() {
    // Given
    doThrow(new ResourceNotFoundException("User not found"))
        .when(commentService).deleteComment(1L, "testuser");

    // When & Then
    assertThatThrownBy(() -> commentController.deleteComment(1L, authentication))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");

    verify(commentService).deleteComment(1L, "testuser");
  }

  @Test
  void testAddComment_WithEmptyContent_ShouldCreateCommentSuccessfully() {
    // Given
    CreateCommentRequest emptyRequest = new CreateCommentRequest(1L, "");
    Comment emptyComment = new Comment();
    emptyComment.setCommentId(2L);
    emptyComment.setTask(testTask);
    emptyComment.setAuthor(testUser);
    emptyComment.setContent("");
    emptyComment.setCreatedAt(LocalDateTime.now());

    CommentResponse emptyResponse = new CommentResponse(
        2L,
        1L,
        1L,
        "testuser",
        "",
        LocalDateTime.now());

    when(commentService.addComment(emptyRequest, "testuser")).thenReturn(emptyComment);
    when(commentMapper.toResponse(emptyComment)).thenReturn(emptyResponse);

    // When
    ResponseEntity<ResponseWrapper<CommentResponse>> response = commentController.addComment(emptyRequest,
        authentication);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData().content()).isEqualTo("");

    verify(commentService).addComment(emptyRequest, "testuser");
    verify(commentMapper).toResponse(emptyComment);
  }

  @Test
  void testAddComment_WithNullContent_ShouldCreateCommentSuccessfully() {
    // Given
    CreateCommentRequest nullRequest = new CreateCommentRequest(1L, null);
    Comment nullComment = new Comment();
    nullComment.setCommentId(3L);
    nullComment.setTask(testTask);
    nullComment.setAuthor(testUser);
    nullComment.setContent(null);
    nullComment.setCreatedAt(LocalDateTime.now());

    CommentResponse nullResponse = new CommentResponse(
        3L,
        1L,
        1L,
        "testuser",
        null,
        LocalDateTime.now());

    when(commentService.addComment(nullRequest, "testuser")).thenReturn(nullComment);
    when(commentMapper.toResponse(nullComment)).thenReturn(nullResponse);

    // When
    ResponseEntity<ResponseWrapper<CommentResponse>> response = commentController.addComment(nullRequest,
        authentication);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData().content()).isNull();

    verify(commentService).addComment(nullRequest, "testuser");
    verify(commentMapper).toResponse(nullComment);
  }

  @Test
  void testAddComment_WithNullAuthentication_ShouldThrowException() {
    // Given
    Authentication nullAuth = null;

    // When & Then
    assertThatThrownBy(() -> commentController.addComment(createRequest, nullAuth))
        .isInstanceOf(NullPointerException.class);

    verify(commentService, never()).addComment(any(), anyString());
  }

  @Test
  void testGetTaskComments_WithNonExistentTask_ShouldReturnEmptyList() {
    // Given
    when(commentService.getCommentsByTask(999L)).thenReturn(List.of());

    // When
    ResponseEntity<ResponseWrapper<List<CommentResponse>>> response = commentController.getTaskComments(999L);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEmpty();

    verify(commentService).getCommentsByTask(999L);
    verify(commentMapper, never()).toResponse(any());
  }
}
