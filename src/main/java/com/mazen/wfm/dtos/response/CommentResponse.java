package com.mazen.wfm.dtos.response;

import java.time.LocalDateTime;

public record CommentResponse (
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {}
