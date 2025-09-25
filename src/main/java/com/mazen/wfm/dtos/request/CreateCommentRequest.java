package com.mazen.wfm.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull(message = "task id cannot be blank")
        Long taskId,
        @NotBlank(message = "comment content cannot be blank")
        String content
) {}