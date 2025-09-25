package com.mazen.wfm.mapper;

import com.mazen.wfm.dtos.TagDTO;
import com.mazen.wfm.dtos.TaskSummaryDTO;
import com.mazen.wfm.dtos.request.TaskAdviceRequest;
import com.mazen.wfm.dtos.request.TaskRequest;
import com.mazen.wfm.dtos.response.TaskResponse;
import com.mazen.wfm.dtos.response.UserResponse;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.Tag;
import com.mazen.wfm.models.Task;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {
    // Entity -> Response
    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "parentTask", expression = "java(toSummary(task.getParentTask()))")
    @Mapping(target = "assignees", expression = "java(mapUsersToResponses(task.getAssignees()))")
    @Mapping(target = "tags", expression = "java(mapTagsToDTOS(task.getTags()))")
    TaskResponse toResponse(Task task);

    // Request -> Entity (stub entities for relationships)
    @Mapping(target = "project", source = "projectId", qualifiedByName = "mapProject")
    @Mapping(target = "parentTask", source = "parentTaskId", qualifiedByName = "mapTask")
    @Mapping(target = "tags", source = "tagIds", qualifiedByName = "mapTags")
    Task toEntity(TaskRequest request);

    @Mapping(target = "project", source = "projectId", qualifiedByName = "mapProject")
    @Mapping(target = "parentTask", source = "parentTaskId", qualifiedByName = "mapTask")
    @Mapping(target = "tags", source = "tagIds", qualifiedByName = "mapTags")
    void updateEntityFromRequest(TaskRequest request, @MappingTarget Task task);


    // This method maps a single object
    TaskAdviceRequest toTaskAdviceRequest(TaskResponse taskResponse);

    // This method will automatically handle mapping a list of objects
    List<TaskAdviceRequest> toTaskAdviceRequestList(List<TaskResponse> tasks);

    // --- Helpers ---
    default TaskSummaryDTO toSummary(Task task) {
        if (task == null) return null;
        return new TaskSummaryDTO(task.getTaskId(), task.getTitle());
    }

    default UserResponse toUserResponse(AppUser user) {
        return new UserResponse(user.getUserId(), user.getUsername(), user.getFullName(), user.getEmail());
    }

    default TagDTO toTagDto(Tag tag) {
        return new TagDTO(tag.getTagId(), tag.getName());
    }

    default Set<UserResponse> mapUsersToResponses(Set<AppUser> users) {
        if (users == null) return Set.of();
        return users.stream().map(this::toUserResponse).collect(Collectors.toSet());
    }

    default Set<TagDTO> mapTagsToDTOS(Set<Tag> tags) {
        if (tags == null) return Set.of();
        return tags.stream().map(this::toTagDto).collect(Collectors.toSet());
    }

    @Named("mapProject")
    default Project mapProject(Long id) {
        if (id == null) return null;
        Project p = new Project();
        p.setProjectId(id);
        return p;
    }

    @Named("mapTask")
    default Task mapTask(Long id) {
        if (id == null) return null;
        Task t = new Task();
        t.setTaskId(id);
        return t;
    }

    @Named("mapUsers")
    default Set<AppUser> mapUsers(Set<Long> ids) {
        if (ids == null) return Set.of();
        return ids.stream().map(id -> {
            AppUser u = new AppUser();
            u.setUserId(id);
            return u;
        }).collect(Collectors.toSet());
    }

    @Named("mapTags")
    default Set<Tag> mapTags(Set<Long> ids) {
        if (ids == null) return Set.of();
        return ids.stream().map(id -> {
            Tag t = new Tag();
            t.setTagId(id);
            return t;
        }).collect(Collectors.toSet());
    }

}