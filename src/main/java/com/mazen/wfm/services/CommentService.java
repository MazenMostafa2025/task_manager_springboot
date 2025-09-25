package com.mazen.wfm.services;

import com.mazen.wfm.dtos.request.CommentRequest;
import com.mazen.wfm.dtos.request.CreateCommentRequest;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Comment;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.CommentRepository;
import com.mazen.wfm.repositories.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.commentRepository = commentRepository;
        this.appUserRepository = appUserRepository;
        this.taskRepository = taskRepository;
    }

    public Comment addComment(CreateCommentRequest request, String username) {
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not Found"));
        AppUser author = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not Found"));
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setTask(task);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    public List<Comment> getCommentsByTask(Long taskId) {
        return commentRepository.findByTask_TaskId(taskId);
    }

//    public List<Comment> getCommentsByUser(Long userId) {
//        return commentRepository.findByAuthor_UserId(userId);
//    }

    public void deleteComment(Long commentId, String username) {
        AppUser currentUser = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = this.getCommentById(commentId);
        if (!comment.getAuthor().getUserId().equals(currentUser.getUserId()) && currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("You are not allowed to delete this comment");
        commentRepository.deleteById(commentId);
    }

}
