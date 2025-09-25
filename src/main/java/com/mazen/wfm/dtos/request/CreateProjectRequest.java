package com.mazen.wfm.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
        @NotNull(message = "Project name is required")
        @NotBlank(message = "Project name is required")
        String name,
        String description
){}
