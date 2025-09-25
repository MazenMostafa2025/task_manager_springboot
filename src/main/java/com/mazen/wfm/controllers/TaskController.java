package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.TaskAdviceRequest;
import com.mazen.wfm.dtos.request.TaskRequest;
import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.dtos.response.TaskResponse;
import com.mazen.wfm.dtos.response.Wrappers;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.TaskMapper;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Status;
import com.mazen.wfm.models.Task;
import com.mazen.wfm.services.GeminiService;
import com.mazen.wfm.services.TaskService;
import com.mazen.wfm.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.ResponseEntity.*;

@Tag(name = "Tasks", description = "API for Tasks CRUD Operations")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final GeminiService geminiService;
    private final UserService userService;

    public TaskController(TaskService taskService, TaskMapper taskMapper, GeminiService geminiService, UserService userService) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.geminiService = geminiService;
        this.userService = userService;
    }

    @Operation(summary = "get a certain task data by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "task retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTask.class))),
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<TaskResponse>> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ok(ResponseWrapper.success(taskMapper.toResponse(task)));
    }


    @Operation(summary = "get tasks by project and status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "tasks retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTaskList.class))),
    })
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<ResponseWrapper<List<TaskResponse>>> getTasksByProjectAndStatus(@PathVariable Long projectId, @PathVariable Status status) {
        List<TaskResponse> tasks = taskService.getTasksByProjectAndStatus(projectId, status).stream().map(taskMapper::toResponse).toList();
        return ResponseEntity.ok(ResponseWrapper.success(tasks));
    }

    @Operation(summary = "get tasks for a certain project with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "tasks retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTaskList.class))),
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseWrapper<Page<TaskResponse>>> getTasksByProject(@PathVariable Long projectId, Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, pageable).map(taskMapper::toResponse);
        return ok(ResponseWrapper.success(tasks));
    }

    @Operation(summary = "get tasks of a certain user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "tasks retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTaskList.class))),
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseWrapper<List<TaskResponse>>> getTasksByUser(@PathVariable("userId") Long userId) {
        List<TaskResponse> tasks = taskService.getTasksByUser(userId).stream().map(taskMapper::toResponse).toList();
        return ok(ResponseWrapper.success(tasks));
    }

    @Operation(summary = "Create a task related to a certain project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "task created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTask.class))),
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<TaskResponse>> createTask(@RequestBody TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task saved = taskService.createTask(task);
        return status(HttpStatus.CREATED).body(ResponseWrapper.success(taskMapper.toResponse(saved)));
    }

    @Operation(summary = "assign tags to certain task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "updated task with new tags retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTask.class))),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundResponse")
    })
    @PostMapping("/{id}/tags")
    public ResponseEntity<ResponseWrapper<TaskResponse>> AssignTagsToTask(@PathVariable("id") Long taskId, @RequestBody Set<Long> tagIds) {
        Task task = taskService.assignTags(tagIds, taskId);
        return ResponseEntity.ok(ResponseWrapper.success(taskMapper.toResponse(task)));
    }

    @Operation(summary = "get advice of current tasks from gemini api")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks Advice received",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class))),
    })
    @PostMapping("/tasks-advice")
    public ResponseEntity<ResponseWrapper<String>> getTasksAdvice (Authentication authentication) {
        String username = authentication.getName();
        AppUser user = userService.findByUsername(username);
        List<Status> statuses = List.of(Status.TODO, Status.IN_PROGRESS);
        List<TaskResponse> tasksResponses = taskService.getTasksByUserAndStatus(user.getUserId(), statuses).stream().map(taskMapper::toResponse).toList();
        List<TaskAdviceRequest> tasks = taskMapper.toTaskAdviceRequestList(tasksResponses);
        if (tasks.isEmpty())
            throw new ResourceNotFoundException("No tasks found");
        String message = geminiService.manageTasks(tasks);
        return ok(ResponseWrapper.success(message));
    }

    @Operation(summary = "Assign User to a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "user assigned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTask.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse")
    })
    @PostMapping("/{task_id}/users")
    public ResponseEntity<ResponseWrapper<TaskResponse>> assignUsers(@PathVariable("task_id") Long taskId, @RequestBody Set<Long> users_ids) {
        Task task = taskService.assignUsersToTask(taskId, users_ids);
        return ResponseEntity.ok(ResponseWrapper.success(taskMapper.toResponse(task)));
    }

    @Operation(summary = "Update task data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "task updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperTask.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundResponse")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseWrapper<TaskResponse>> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        Task updatedTask = taskService.updateTask(id, request);
        TaskResponse taskResponse = taskMapper.toResponse(updatedTask);
        return ok(ResponseWrapper.success(taskResponse));
    }


    @Operation(summary = "delete all tags of a task by task id")
    @ApiResponse(responseCode = "204", description = "tags Deleted", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/{id}/assignees")
    public ResponseEntity<Void> clearTaskAssignees(@PathVariable Long id) {
        taskService.clearAssignees(id);
        return noContent().build();
    }

    @Operation(summary = "delete all tags of a task by task id")
    @ApiResponse(responseCode = "204", description = "tags Deleted", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/{id}/tags")
    public ResponseEntity<Void> clearTaskTags(@PathVariable Long id) {
        taskService.clearTags(id);
        return noContent().build();
    }

    @Operation(summary = "delete a task by task id")
    @ApiResponse(responseCode = "204", description = "Project Deleted", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return noContent().build();
    }
}
