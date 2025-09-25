package com.mazen.wfm.dtos.request;

import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Status;
import java.time.LocalDate;
import java.util.Set;

public record TaskRequest(
        String title,
        String description,
        Status status,
        Priority priority,
        LocalDate dueDate,
        Long projectId,
        Long parentTaskId,
        Set<Long> tagIds
) {}
