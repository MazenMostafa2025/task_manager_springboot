package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.CreateCommentRequest;
import com.mazen.wfm.dtos.response.CommentResponse;
import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.dtos.response.Wrappers;
import com.mazen.wfm.mapper.CommentMapper;
import com.mazen.wfm.models.Comment;
import com.mazen.wfm.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;


@Tag(name = "Comments", description = "API for Comments CRUD Operations")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @Operation(summary = "get comments of a certain task using task id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperCommentList.class))),
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ResponseWrapper<List<CommentResponse>>> getTaskComments(@PathVariable Long taskId) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId).stream().map(commentMapper::toResponse).toList();
        return ok(ResponseWrapper.success(comments));
    }

    @Operation(summary = "comment on a certain task")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperComment.class))),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundResponse")
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<CommentResponse>> addComment(@Valid @RequestBody CreateCommentRequest request, Authentication authentication) {
        String username = authentication.getName();
        Comment comment = commentService.addComment(request, username);
        CommentResponse commentResponse = commentMapper.toResponse(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(commentResponse));
    }



//    @Operation(summary = "get comments of a certain user")
//    @GetMapping("/user/{userId}")
//    public List<Comment> getCommentsByUser(@PathVariable Long userId) {
//        return commentService.getCommentsByUser(userId);
//    }

    @Operation(summary = "delete a certain comment")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(id, username);
        return noContent().build();
    }
}
