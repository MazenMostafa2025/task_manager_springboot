package com.mazen.wfm.mapper;

import com.mazen.wfm.dtos.ProjectDTO;
import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.models.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.stream.Collectors;
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {
//    @Mapping(target = "ownerId", source = "owner.userId")
//    @Mapping(target = "taskIds", expression = "java(project.getTasks().stream().map(t -> t.getTaskId()).collect(Collectors.toSet()))")
//    ProjectDTO toDto(Project project);
    // Create: convert request → entity (owner is set in the service, not here)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "owner", ignore = true) // will be set in the service
    Project toEntity(CreateProjectRequest request);

    // Update: apply changes from request → existing entity
    void updateEntityFromRequest(UpdateProjectRequest request, @MappingTarget Project project);

    // Convert entity → response DTO
    @Mapping(target = "ownerId", source = "owner.userId")
    @Mapping(target = "ownerName", source = "owner.fullName")
    ProjectResponse toResponse(Project project);


}