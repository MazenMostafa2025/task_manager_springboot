package com.mazen.wfm.dtos;

import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TaskDTO(
        Long taskId,
        String title,
        String description,
        Status status,
        Priority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long projectId,
        Long parentTaskId,
        Set<Long> assigneeIds,
        Set<Long> tagIds
) {}