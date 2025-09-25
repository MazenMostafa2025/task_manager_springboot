package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.dtos.response.Wrappers;
import com.mazen.wfm.mapper.ProjectMapper;
import com.mazen.wfm.dtos.response.Wrappers.ResponseWrapperProject;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.services.ProjectService;
import com.mazen.wfm.dtos.response.ResponseWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Projects", description = "API for Projects CRUD Operations")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @Operation(summary = "get a project using project id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapperProject.class))),
            @ApiResponse(responseCode = "404",ref = "#/components/responses/NotFoundResponse")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProjectResponse>> getProject(@PathVariable Long id) {
        ProjectResponse projectResponse = projectMapper.toResponse(projectService.getProjectById(id));
        return ResponseEntity.ok(ResponseWrapper.success(projectResponse));
    }
    @Operation(summary = "search for projects using project name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieving projects per user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperProjectList.class)))
    })
    @GetMapping("/search")
    public ResponseWrapper<List<ProjectResponse>> searchProjects(@RequestParam String name) {
        return ResponseWrapper.success(projectService.searchProjectsByName(name).stream().map(projectMapper::toResponse).toList());
    }

    @Operation(summary = "get projects for a certain user using user id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrieving projects per user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Wrappers.ResponseWrapperProjectList.class))),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundResponse")
    })
    @GetMapping("/my")
    public ResponseEntity<ResponseWrapper<List<ProjectResponse>>> getUserProjects(Authentication authentication) {
        List<ProjectResponse> projectResponses = projectService.getProjectsByUserName(authentication.getName()).stream().map(projectMapper::toResponse).toList();
        return ResponseEntity.ok(ResponseWrapper.success(projectResponses));
    }

    @Operation(summary = "create a project")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapperProject.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse")
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request, Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse projectResponse = projectService.createProject(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success("done", projectResponse));
    }

    @Operation(summary = "update project data using project id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update a project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapperProject.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProjectResponse>> updateProject(@PathVariable Long id, @RequestBody UpdateProjectRequest request) {
        Project project = projectService.updateProject(id, request);
        ProjectResponse projectResponse = projectMapper.toResponse(project);
        return ResponseEntity.ok(ResponseWrapper.success(projectResponse));
    }

    @Operation(summary = "delete a project by project id")
    @ApiResponse(responseCode = "204", description = "Project Deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        projectService.deleteProject(username, id);
        return ResponseEntity.noContent().build();
    }
}
