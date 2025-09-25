package com.mazen.wfm.dtos;

import com.mazen.wfm.models.UserRole;
import java.time.LocalDateTime;

public record AppUserDTO(
        Long userId,
        String username,
        String fullName,
        String email,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt
) {}