package com.mazen.wfm.mapper;

import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        projectMapper = Mappers.getMapper(ProjectMapper.class);
    }

    @Test
    void testToEntity_FromCreateProjectRequest_ShouldMapCorrectly() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Test Project", "Test Description");

        // When
        Project project = projectMapper.toEntity(request);

        // Then
        assertThat(project).isNotNull();
        assertThat(project.getProjectId()).isNull(); // Should be ignored
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getDescription()).isEqualTo("Test Description");
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getTasks()).isEmpty(); // Should be ignored
        assertThat(project.getOwner()).isNull(); // Should be ignored
    }

    @Test
    void testToEntity_FromCreateProjectRequestWithNullDescription_ShouldMapCorrectly() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Test Project", null);

        // When
        Project project = projectMapper.toEntity(request);

        // Then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getDescription()).isNull();
        assertThat(project.getCreatedAt()).isNotNull();
    }

    @Test
    void testUpdateEntityFromRequest_ShouldUpdateOnlyProvidedFields() {
        // Given
        Project existingProject = new Project();
        existingProject.setProjectId(1L);
        existingProject.setName("Original Name");
        existingProject.setDescription("Original Description");
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingProject.setOwner(createTestUser());

        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated Description");

        // When
        projectMapper.updateEntityFromRequest(request, existingProject);

        // Then
        assertThat(existingProject.getProjectId()).isEqualTo(1L); // Should not change
        assertThat(existingProject.getName()).isEqualTo("Updated Name");
        assertThat(existingProject.getDescription()).isEqualTo("Updated Description");
        assertThat(existingProject.getCreatedAt()).isNotNull(); // Should not change
        assertThat(existingProject.getOwner()).isNotNull(); // Should not change
    }

    @Test
    void testUpdateEntityFromRequest_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        Project existingProject = new Project();
        existingProject.setProjectId(1L);
        existingProject.setName("Original Name");
        existingProject.setDescription("Original Description");
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingProject.setOwner(createTestUser());

        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", null);

        // When
        projectMapper.updateEntityFromRequest(request, existingProject);

        // Then
        assertThat(existingProject.getName()).isEqualTo("Updated Name");
        assertThat(existingProject.getDescription()).isEqualTo("Original Description"); // Should not change
    }

    @Test
    void testUpdateEntityFromRequest_WithNullValues_ShouldNotUpdateFields() {
        // Given
        Project existingProject = new Project();
        existingProject.setProjectId(1L);
        existingProject.setName("Original Name");
        existingProject.setDescription("Original Description");
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingProject.setOwner(createTestUser());

        UpdateProjectRequest request = new UpdateProjectRequest(null, null);

        // When
        projectMapper.updateEntityFromRequest(request, existingProject);

        // Then
        assertThat(existingProject.getName()).isEqualTo("Original Name"); // Should not change
        assertThat(existingProject.getDescription()).isEqualTo("Original Description"); // Should not change
    }

    @Test
    void testToResponse_FromProject_ShouldMapCorrectly() {
        // Given
        AppUser owner = createTestUser();
        Project project = new Project();
        project.setProjectId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(owner);

        // When
        ProjectResponse response = projectMapper.toResponse(project);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.description()).isEqualTo("Test Description");
        assertThat(response.createdAt()).isEqualTo(project.getCreatedAt());
        assertThat(response.ownerId()).isEqualTo(owner.getUserId());
        assertThat(response.ownerName()).isEqualTo(owner.getFullName());
    }

    @Test
    void testToResponse_FromProjectWithNullDescription_ShouldMapCorrectly() {
        // Given
        AppUser owner = createTestUser();
        Project project = new Project();
        project.setProjectId(1L);
        project.setName("Test Project");
        project.setDescription(null);
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(owner);

        // When
        ProjectResponse response = projectMapper.toResponse(project);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.description()).isNull();
        assertThat(response.ownerId()).isEqualTo(owner.getUserId());
        assertThat(response.ownerName()).isEqualTo(owner.getFullName());
    }

    @Test
    void testToResponse_FromProjectWithNullOwner_ShouldMapCorrectly() {
        // Given
        Project project = new Project();
        project.setProjectId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(null);

        // When
        ProjectResponse response = projectMapper.toResponse(project);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.description()).isEqualTo("Test Description");
        assertThat(response.ownerId()).isNull();
        assertThat(response.ownerName()).isNull();
    }

    @Test
    void testToEntity_ShouldSetCurrentTimestamp() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Test Project", "Test Description");
        LocalDateTime beforeMapping = LocalDateTime.now();

        // When
        Project project = projectMapper.toEntity(request);
        LocalDateTime afterMapping = LocalDateTime.now();

        // Then
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getCreatedAt()).isAfter(beforeMapping.minusSeconds(1));
        assertThat(project.getCreatedAt()).isBefore(afterMapping.plusSeconds(1));
    }

    @Test
    void testUpdateEntityFromRequest_WithEmptyStringValues_ShouldUpdateFields() {
        // Given
        Project existingProject = new Project();
        existingProject.setProjectId(1L);
        existingProject.setName("Original Name");
        existingProject.setDescription("Original Description");
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingProject.setOwner(createTestUser());

        UpdateProjectRequest request = new UpdateProjectRequest("", "");

        // When
        projectMapper.updateEntityFromRequest(request, existingProject);

        // Then
        assertThat(existingProject.getName()).isEqualTo("");
        assertThat(existingProject.getDescription()).isEqualTo("");
    }

    private AppUser createTestUser() {
        return AppUser.builder()
                .userId(1L)
                .username("testuser")
                .password("password")
                .fullName("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
