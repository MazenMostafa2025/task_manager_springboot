package com.mazen.wfm.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record ProjectDTO(
        Long projectId,
        String name,
        String description,
        LocalDateTime createdAt,
        Long ownerId,
        Set<Long> taskIds
) {}