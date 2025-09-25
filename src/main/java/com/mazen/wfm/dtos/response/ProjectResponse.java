package com.mazen.wfm.dtos.response;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long projectId,
        String name,
        String description,
        LocalDateTime createdAt,
        Long ownerId,
        String ownerName
) {}
