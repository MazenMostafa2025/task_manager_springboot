package com.mazen.wfm.services;

import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser1;
    private AppUser testUser2;
    private AppUser adminUser;
    private Project testProject1;
    private Project testProject2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        projectRepository.deleteAll();
        appUserRepository.deleteAll();

        // Create test users
        testUser1 = AppUser.builder()
                .username("user1")
                .password("password")
                .fullName("User One")
                .email("user1@test.com")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        testUser2 = AppUser.builder()
                .username("user2")
                .password("password")
                .fullName("User Two")
                .email("user2@test.com")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        adminUser = AppUser.builder()
                .username("admin")
                .password("password")
                .fullName("Admin User")
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Persist users
        testUser1 = appUserRepository.save(testUser1);
        testUser2 = appUserRepository.save(testUser2);
        adminUser = appUserRepository.save(adminUser);

        // Create test projects
        testProject1 = new Project();
        testProject1.setName("Test Project 1");
        testProject1.setDescription("Description for project 1");
        testProject1.setCreatedAt(LocalDateTime.now());
        testProject1.setOwner(testUser1);

        testProject2 = new Project();
        testProject2.setName("Test Project 2");
        testProject2.setDescription("Description for project 2");
        testProject2.setCreatedAt(LocalDateTime.now());
        testProject2.setOwner(testUser2);

        // Persist projects
        testProject1 = projectRepository.save(testProject1);
        testProject2 = projectRepository.save(testProject2);
    }

    @Test
    void testCreateProject_ShouldCreateProjectSuccessfully() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("New Project", "New project description");

        // When
        ProjectResponse response = projectService.createProject(request, testUser1.getUsername());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("New Project");
        assertThat(response.description()).isEqualTo("New project description");
        assertThat(response.ownerId()).isEqualTo(testUser1.getUserId());
        assertThat(response.ownerName()).isEqualTo(testUser1.getFullName());
        assertThat(response.createdAt()).isNotNull();

        // Verify project is persisted in database
        List<Project> userProjects = projectRepository.findByOwner_UserId(testUser1.getUserId());
        assertThat(userProjects).hasSize(2); // Original + new project
        assertThat(userProjects).extracting(Project::getName)
                .contains("New Project");
    }

    @Test
    void testCreateProject_WithNonExistentUser_ShouldThrowException() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("New Project", "New project description");

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(request, "nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Owner not found");
    }

    @Test
    void testGetProjectById_ShouldReturnProjectSuccessfully() {
        // When
        Project project = projectService.getProjectById(testProject1.getProjectId());

        // Then
        assertThat(project).isNotNull();
        assertThat(project.getProjectId()).isEqualTo(testProject1.getProjectId());
        assertThat(project.getName()).isEqualTo("Test Project 1");
        assertThat(project.getDescription()).isEqualTo("Description for project 1");
        assertThat(project.getOwner().getUserId()).isEqualTo(testUser1.getUserId());
    }

    @Test
    void testGetProjectById_WithNonExistentId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");
    }

    @Test
    void testGetProjectsByUserName_ShouldReturnUserProjects() {
        // When
        List<Project> user1Projects = projectService.getProjectsByUserName(testUser1.getUsername());
        List<Project> user2Projects = projectService.getProjectsByUserName(testUser2.getUsername());

        // Then
        assertThat(user1Projects).hasSize(1);
        assertThat(user1Projects.get(0).getName()).isEqualTo("Test Project 1");
        assertThat(user1Projects.get(0).getOwner().getUserId()).isEqualTo(testUser1.getUserId());

        assertThat(user2Projects).hasSize(1);
        assertThat(user2Projects.get(0).getName()).isEqualTo("Test Project 2");
        assertThat(user2Projects.get(0).getOwner().getUserId()).isEqualTo(testUser2.getUserId());
    }

    @Test
    void testGetProjectsByUserName_WithNonExistentUser_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> projectService.getProjectsByUserName("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User Not Found");
    }

    @Test
    void testSearchProjectsByName_ShouldReturnMatchingProjects() {
        // When
        List<Project> testProjects = projectService.searchProjectsByName("Test");
        List<Project> project1Results = projectService.searchProjectsByName("Project 1");

        // Then
        assertThat(testProjects).hasSize(2);
        assertThat(testProjects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2");

        assertThat(project1Results).hasSize(1);
        assertThat(project1Results.get(0).getName()).isEqualTo("Test Project 1");
    }

    @Test
    void testSearchProjectsByName_WithNoMatches_ShouldReturnEmptyList() {
        // When
        List<Project> noMatches = projectService.searchProjectsByName("Nonexistent");

        // Then
        assertThat(noMatches).isEmpty();
    }

    @Test
    void testSearchProjectsByName_ShouldBeCaseInsensitive() {
        // When
        List<Project> upperCaseResults = projectService.searchProjectsByName("TEST");
        List<Project> lowerCaseResults = projectService.searchProjectsByName("test");

        // Then
        assertThat(upperCaseResults).hasSize(2);
        assertThat(lowerCaseResults).hasSize(2);
        assertThat(upperCaseResults).isEqualTo(lowerCaseResults);
    }

    @Test
    void testUpdateProject_ShouldUpdateProjectSuccessfully() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Project Name", "Updated description");

        // When
        Project updatedProject = projectService.updateProject(testProject1.getProjectId(), request);

        // Then
        assertThat(updatedProject.getName()).isEqualTo("Updated Project Name");
        assertThat(updatedProject.getDescription()).isEqualTo("Updated description");
        assertThat(updatedProject.getProjectId()).isEqualTo(testProject1.getProjectId());

        // Verify changes are persisted
        Project persistedProject = projectRepository.findById(testProject1.getProjectId()).orElseThrow();
        assertThat(persistedProject.getName()).isEqualTo("Updated Project Name");
        assertThat(persistedProject.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testUpdateProject_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Only Name Updated", null);

        // When
        Project updatedProject = projectService.updateProject(testProject1.getProjectId(), request);

        // Then
        assertThat(updatedProject.getName()).isEqualTo("Only Name Updated");
        assertThat(updatedProject.getDescription()).isEqualTo("Description for project 1"); // Original description preserved
    }

    @Test
    void testUpdateProject_WithNonExistentId_ShouldThrowException() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated description");

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");
    }

    @Test
    void testDeleteProject_ByOwner_ShouldDeleteProjectSuccessfully() {
        // Given
        Long projectId = testProject1.getProjectId();

        // When
        projectService.deleteProject(testUser1.getUsername(), projectId);

        // Then
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    void testDeleteProject_ByAdmin_ShouldDeleteProjectSuccessfully() {
        // Given
        Long projectId = testProject1.getProjectId();

        // When
        projectService.deleteProject(adminUser.getUsername(), projectId);

        // Then
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    void testDeleteProject_ByNonOwner_ShouldThrowException() {
        // Given
        Long projectId = testProject1.getProjectId();

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(testUser2.getUsername(), projectId))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("You are not the owner of the project");
    }

    @Test
    void testDeleteProject_WithNonExistentUser_ShouldThrowException() {
        // Given
        Long projectId = testProject1.getProjectId();

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject("nonexistent", projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User Not Found");
    }

    @Test
    void testDeleteProject_WithNonExistentProject_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(testUser1.getUsername(), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project Not Found");
    }

    @Test
    void testCreateProject_ShouldSetCorrectTimestamps() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Timestamp Test Project", "Testing timestamps");
        LocalDateTime beforeCreation = LocalDateTime.now();

        // When
        ProjectResponse response = projectService.createProject(request, testUser1.getUsername());
        LocalDateTime afterCreation = LocalDateTime.now();

        // Then
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.createdAt()).isAfter(beforeCreation);
        assertThat(response.createdAt()).isBefore(afterCreation);
    }

    @Test
    void testGetProjectsByUserName_WithMultipleProjects_ShouldReturnAllUserProjects() {
        // Given - Create additional project for user1
        Project additionalProject = new Project();
        additionalProject.setName("Additional Project");
        additionalProject.setDescription("Additional project description");
        additionalProject.setCreatedAt(LocalDateTime.now());
        additionalProject.setOwner(testUser1);
        projectRepository.save(additionalProject);

        // When
        List<Project> user1Projects = projectService.getProjectsByUserName(testUser1.getUsername());

        // Then
        assertThat(user1Projects).hasSize(2);
        assertThat(user1Projects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Additional Project");
    }
}