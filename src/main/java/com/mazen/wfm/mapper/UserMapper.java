package com.mazen.wfm.mapper;

import com.mazen.wfm.dto.auth.RegisterRequest;
import com.mazen.wfm.dtos.response.UserResponse;
import com.mazen.wfm.models.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    UserResponse toResponse(AppUser user);
    AppUser toEntity(RegisterRequest request);

}