package com.mazen.wfm.repositories;

import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryUnitTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser1;
    private AppUser testUser2;
    private Project testProject1;
    private Project testProject2;
    private Project testProject3;

    @BeforeEach
    void setUp() {
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

        // Persist users
        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);

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
        testProject2.setOwner(testUser1);

        testProject3 = new Project();
        testProject3.setName("Another Project");
        testProject3.setDescription("Description for project 3");
        testProject3.setCreatedAt(LocalDateTime.now());
        testProject3.setOwner(testUser2);

        // Persist projects
        testProject1 = entityManager.persistAndFlush(testProject1);
        testProject2 = entityManager.persistAndFlush(testProject2);
        testProject3 = entityManager.persistAndFlush(testProject3);
    }

    @Test
    void testFindByOwner_UserId_ShouldReturnProjectsForSpecificUser() {
        // When
        List<Project> user1Projects = projectRepository.findByOwner_UserId(testUser1.getUserId());
        List<Project> user2Projects = projectRepository.findByOwner_UserId(testUser2.getUserId());

        // Then
        assertThat(user1Projects).hasSize(2);
        assertThat(user1Projects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2");
        assertThat(user1Projects).extracting(Project::getOwner)
                .allMatch(owner -> owner.getUserId().equals(testUser1.getUserId()));

        assertThat(user2Projects).hasSize(1);
        assertThat(user2Projects).extracting(Project::getName)
                .containsExactly("Another Project");
        assertThat(user2Projects).extracting(Project::getOwner)
                .allMatch(owner -> owner.getUserId().equals(testUser2.getUserId()));
    }

    @Test
    void testFindByOwner_UserId_ShouldReturnEmptyListForNonExistentUser() {
        // When
        List<Project> nonExistentUserProjects = projectRepository.findByOwner_UserId(999L);

        // Then
        assertThat(nonExistentUserProjects).isEmpty();
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldReturnMatchingProjects() {
        // When
        List<Project> projectsWithTest = projectRepository.findByNameContainingIgnoreCase("test");
        List<Project> projectsWithProject = projectRepository.findByNameContainingIgnoreCase("project");
        List<Project> projectsWithAnother = projectRepository.findByNameContainingIgnoreCase("another");

        // Then
        assertThat(projectsWithTest).hasSize(2);
        assertThat(projectsWithTest).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2");

        assertThat(projectsWithProject).hasSize(3);
        assertThat(projectsWithProject).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2", "Another Project");

        assertThat(projectsWithAnother).hasSize(1);
        assertThat(projectsWithAnother).extracting(Project::getName)
                .containsExactly("Another Project");
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        // When
        List<Project> upperCaseResults = projectRepository.findByNameContainingIgnoreCase("TEST");
        List<Project> lowerCaseResults = projectRepository.findByNameContainingIgnoreCase("test");
        List<Project> mixedCaseResults = projectRepository.findByNameContainingIgnoreCase("TeSt");

        // Then
        assertThat(upperCaseResults).hasSize(2);
        assertThat(lowerCaseResults).hasSize(2);
        assertThat(mixedCaseResults).hasSize(2);
        assertThat(upperCaseResults).isEqualTo(lowerCaseResults);
        assertThat(lowerCaseResults).isEqualTo(mixedCaseResults);
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldReturnEmptyListForNonMatchingName() {
        // When
        List<Project> nonMatchingResults = projectRepository.findByNameContainingIgnoreCase("nonexistent");

        // Then
        assertThat(nonMatchingResults).isEmpty();
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldHandleEmptyString() {
        // When
        List<Project> emptyStringResults = projectRepository.findByNameContainingIgnoreCase("");

        // Then
        assertThat(emptyStringResults).hasSize(3);
        assertThat(emptyStringResults).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2", "Another Project");
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldHandleNullString() {
        // When
        List<Project> nullStringResults = projectRepository.findByNameContainingIgnoreCase(null);

        // Then
        assertThat(nullStringResults).isEmpty();
    }

    @Test
    void testSaveProject_ShouldPersistProjectCorrectly() {
        // Given
        AppUser newUser = AppUser.builder()
                .username("newuser")
                .password("password")
                .fullName("New User")
                .email("newuser@test.com")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        Project newProject = new Project();
        newProject.setName("New Test Project");
        newProject.setDescription("New project description");
        newProject.setCreatedAt(LocalDateTime.now());
        newProject.setOwner(newUser);

        // When
        Project savedProject = projectRepository.save(newProject);
        entityManager.flush();

        // Then
        assertThat(savedProject.getProjectId()).isNotNull();
        assertThat(savedProject.getName()).isEqualTo("New Test Project");
        assertThat(savedProject.getDescription()).isEqualTo("New project description");
        assertThat(savedProject.getOwner().getUserId()).isEqualTo(newUser.getUserId());

        // Verify it's actually persisted
        Optional<Project> foundProject = projectRepository.findById(savedProject.getProjectId());
        assertThat(foundProject).isPresent();
        assertThat(foundProject.get()).isEqualTo(savedProject);
    }

    @Test
    void testDeleteProject_ShouldRemoveProjectFromDatabase() {
        // Given
        Long projectId = testProject1.getProjectId();

        // When
        projectRepository.deleteById(projectId);
        entityManager.flush();

        // Then
        Optional<Project> deletedProject = projectRepository.findById(projectId);
        assertThat(deletedProject).isEmpty();
    }

    @Test
    void testFindById_ShouldReturnCorrectProject() {
        // When
        Optional<Project> foundProject = projectRepository.findById(testProject1.getProjectId());

        // Then
        assertThat(foundProject).isPresent();
        assertThat(foundProject.get()).isEqualTo(testProject1);
        assertThat(foundProject.get().getName()).isEqualTo("Test Project 1");
    }

    @Test
    void testFindById_ShouldReturnEmptyForNonExistentId() {
        // When
        Optional<Project> foundProject = projectRepository.findById(999L);

        // Then
        assertThat(foundProject).isEmpty();
    }

    @Test
    void testFindAll_ShouldReturnAllProjects() {
        // When
        List<Project> allProjects = projectRepository.findAll();

        // Then
        assertThat(allProjects).hasSize(3);
        assertThat(allProjects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Test Project 1", "Test Project 2", "Another Project");
    }
}
