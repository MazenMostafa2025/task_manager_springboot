package com.mazen.wfm.dtos;

import java.time.LocalDateTime;

public record CommentDTO(
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {}
