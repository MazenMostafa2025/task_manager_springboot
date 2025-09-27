package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.ProjectMapper;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerUnitTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectController projectController;

    private AppUser testUser;
    private Project testProject;
    private ProjectResponse testProjectResponse;
    private CreateProjectRequest createRequest;
    private UpdateProjectRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .userId(1L)
                .username("testuser")
                .password("password")
                .fullName("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        testProject = new Project();
        testProject.setProjectId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setOwner(testUser);

        testProjectResponse = new ProjectResponse(
                1L,
                "Test Project",
                "Test Description",
                LocalDateTime.now(),
                1L,
                "Test User"
        );

        createRequest = new CreateProjectRequest("New Project", "New Description");
        updateRequest = new UpdateProjectRequest("Updated Project", "Updated Description");

        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetProject_ShouldReturnProjectSuccessfully() {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

        // When
        ResponseEntity<ResponseWrapper<ProjectResponse>> response = projectController.getProject(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(testProjectResponse);
        assertThat(response.getBody().getMessage()).isEqualTo("Operation successful");

        verify(projectService).getProjectById(1L);
        verify(projectMapper).toResponse(testProject);
    }

    @Test
    void testGetProject_WithNonExistentId_ShouldThrowException() {
        // Given
        when(projectService.getProjectById(999L))
                .thenThrow(new ResourceNotFoundException("Project Not Found"));

        // When & Then
        assertThatThrownBy(() -> projectController.getProject(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");

        verify(projectService).getProjectById(999L);
        verify(projectMapper, never()).toResponse(any());
    }

    @Test
    void testSearchProjects_ShouldReturnMatchingProjects() {
        // Given
        List<Project> projects = List.of(testProject);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
        
        when(projectService.searchProjectsByName("test")).thenReturn(projects);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

        // When
        ResponseWrapper<List<ProjectResponse>> response = projectController.searchProjects("test");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(projectResponses);
        assertThat(response.getMessage()).isEqualTo("Operation successful");

        verify(projectService).searchProjectsByName("test");
        verify(projectMapper).toResponse(testProject);
    }

    @Test
    void testSearchProjects_WithNoMatches_ShouldReturnEmptyList() {
        // Given
        when(projectService.searchProjectsByName("nonexistent")).thenReturn(List.of());

        // When
        ResponseWrapper<List<ProjectResponse>> response = projectController.searchProjects("nonexistent");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("Operation successful");

        verify(projectService).searchProjectsByName("nonexistent");
        verify(projectMapper, never()).toResponse(any());
    }

    @Test
    void testGetUserProjects_ShouldReturnUserProjects() {
        // Given
        List<Project> projects = List.of(testProject);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
        
        when(projectService.getProjectsByUserName("testuser")).thenReturn(projects);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

        // When
        ResponseEntity<ResponseWrapper<List<ProjectResponse>>> response = 
                projectController.getUserProjects(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(projectResponses);

        verify(projectService).getProjectsByUserName("testuser");
        verify(projectMapper).toResponse(testProject);
    }

    @Test
    void testGetUserProjects_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(projectService.getProjectsByUserName("testuser"))
                .thenThrow(new ResourceNotFoundException("User Not Found"));

        // When & Then
        assertThatThrownBy(() -> projectController.getUserProjects(authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User Not Found");

        verify(projectService).getProjectsByUserName("testuser");
        verify(projectMapper, never()).toResponse(any());
    }

    @Test
    void testCreateProject_ShouldCreateProjectSuccessfully() {
        // Given
        when(projectService.createProject(createRequest, "testuser")).thenReturn(testProjectResponse);

        // When
        ResponseEntity<ResponseWrapper<ProjectResponse>> response = 
                projectController.createProject(createRequest, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(testProjectResponse);
        assertThat(response.getBody().getMessage()).isEqualTo("done");

        verify(projectService).createProject(createRequest, "testuser");
    }

    @Test
    void testCreateProject_WithInvalidRequest_ShouldThrowException() {
        // Given
        CreateProjectRequest invalidRequest = new CreateProjectRequest("", "Description");
        when(projectService.createProject(invalidRequest, "testuser"))
                .thenThrow(new IllegalArgumentException("Project name cannot be empty"));

        // When & Then
        assertThatThrownBy(() -> projectController.createProject(invalidRequest, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project name cannot be empty");

        verify(projectService).createProject(invalidRequest, "testuser");
    }

    @Test
    void testUpdateProject_ShouldUpdateProjectSuccessfully() {
        // Given
        Project updatedProject = new Project();
        updatedProject.setProjectId(1L);
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated Description");
        updatedProject.setCreatedAt(LocalDateTime.now());
        updatedProject.setOwner(testUser);

        ProjectResponse updatedResponse = new ProjectResponse(
                1L,
                "Updated Project",
                "Updated Description",
                LocalDateTime.now(),
                1L,
                "Test User"
        );

        when(projectService.updateProject(1L, updateRequest)).thenReturn(updatedProject);
        when(projectMapper.toResponse(updatedProject)).thenReturn(updatedResponse);

        // When
        ResponseEntity<ResponseWrapper<ProjectResponse>> response = 
                projectController.updateProject(1L, updateRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(updatedResponse);

        verify(projectService).updateProject(1L, updateRequest);
        verify(projectMapper).toResponse(updatedProject);
    }

    @Test
    void testUpdateProject_WithNonExistentId_ShouldThrowException() {
        // Given
        when(projectService.updateProject(999L, updateRequest))
                .thenThrow(new ResourceNotFoundException("Project Not Found"));

        // When & Then
        assertThatThrownBy(() -> projectController.updateProject(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");

        verify(projectService).updateProject(999L, updateRequest);
        verify(projectMapper, never()).toResponse(any());
    }

    @Test
    void testDeleteProject_ShouldDeleteProjectSuccessfully() {
        // Given
        doNothing().when(projectService).deleteProject("testuser", 1L);

        // When
        ResponseEntity<Void> response = projectController.deleteProject(1L, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(projectService).deleteProject("testuser", 1L);
    }

    @Test
    void testDeleteProject_WithNonExistentProject_ShouldThrowException() {
        // Given
        doThrow(new ResourceNotFoundException("Project Not Found"))
                .when(projectService).deleteProject("testuser", 999L);

        // When & Then
        assertThatThrownBy(() -> projectController.deleteProject(999L, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");

        verify(projectService).deleteProject("testuser", 999L);
    }

    @Test
    void testDeleteProject_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        doThrow(new org.springframework.security.authorization.AuthorizationDeniedException("Not authorized"))
                .when(projectService).deleteProject("testuser", 1L);

        // When & Then
        assertThatThrownBy(() -> projectController.deleteProject(1L, authentication))
                .isInstanceOf(org.springframework.security.authorization.AuthorizationDeniedException.class)
                .hasMessage("Not authorized");

        verify(projectService).deleteProject("testuser", 1L);
    }

    @Test
    void testSearchProjects_WithEmptySearchTerm_ShouldReturnAllProjects() {
        // Given
        List<Project> allProjects = List.of(testProject);
        List<ProjectResponse> allProjectResponses = List.of(testProjectResponse);
        
        when(projectService.searchProjectsByName("")).thenReturn(allProjects);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

        // When
        ResponseWrapper<List<ProjectResponse>> response = projectController.searchProjects("");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(allProjectResponses);

        verify(projectService).searchProjectsByName("");
        verify(projectMapper).toResponse(testProject);
    }

    @Test
    void testGetUserProjects_WithMultipleProjects_ShouldReturnAllUserProjects() {
        // Given
        Project project2 = new Project();
        project2.setProjectId(2L);
        project2.setName("Project 2");
        project2.setDescription("Description 2");
        project2.setCreatedAt(LocalDateTime.now());
        project2.setOwner(testUser);

        ProjectResponse response2 = new ProjectResponse(
                2L,
                "Project 2",
                "Description 2",
                LocalDateTime.now(),
                1L,
                "Test User"
        );

        List<Project> projects = List.of(testProject, project2);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse, response2);
        
        when(projectService.getProjectsByUserName("testuser")).thenReturn(projects);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);
        when(projectMapper.toResponse(project2)).thenReturn(response2);

        // When
        ResponseEntity<ResponseWrapper<List<ProjectResponse>>> response = 
                projectController.getUserProjects(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData()).isEqualTo(projectResponses);

        verify(projectService).getProjectsByUserName("testuser");
        verify(projectMapper).toResponse(testProject);
        verify(projectMapper).toResponse(project2);
    }

    @Test
    void testCreateProject_WithNullAuthentication_ShouldThrowException() {
        // Given
        Authentication nullAuth = null;

        // When & Then
        assertThatThrownBy(() -> projectController.createProject(createRequest, nullAuth))
                .isInstanceOf(NullPointerException.class);

        verify(projectService, never()).createProject(any(), anyString());
    }

    @Test
    void testUpdateProject_WithNullRequest_ShouldHandleGracefully() {
        // Given
        UpdateProjectRequest nullRequest = null;
        when(projectService.updateProject(1L, nullRequest)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

        // When
        ResponseEntity<ResponseWrapper<ProjectResponse>> response = 
                projectController.updateProject(1L, nullRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();

        verify(projectService).updateProject(1L, nullRequest);
        verify(projectMapper).toResponse(testProject);
    }
}
