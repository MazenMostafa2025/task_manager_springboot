package com.mazen.wfm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProjectControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  private AppUser testUser;
  private Project testProject;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Clean up existing data
    projectRepository.deleteAll();
    appUserRepository.deleteAll();

    // Create test user
    testUser = AppUser.builder()
        .username("testuser")
        .password("password")
        .fullName("Test User")
        .email("test@example.com")
        .role(UserRole.USER)
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();
    testUser = appUserRepository.save(testUser);

    // Create test project
    testProject = new Project();
    testProject.setName("Test Project");
    testProject.setDescription("Test Description");
    testProject.setCreatedAt(LocalDateTime.now());
    testProject.setOwner(testUser);
    testProject = projectRepository.save(testProject);
  }

  @Test
  @WithMockUser(username = "testuser")
  void testGetProject_ShouldReturnProjectSuccessfully() throws Exception {
    mockMvc.perform(get("/api/projects/{id}", testProject.getProjectId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Operation successful"))
        .andExpect(jsonPath("$.data.projectId").value(testProject.getProjectId().intValue()))
        .andExpect(jsonPath("$.data.name").value("Test Project"))
        .andExpect(jsonPath("$.data.description").value("Test Description"))
        .andExpect(jsonPath("$.data.ownerId").value(testUser.getUserId().intValue()))
        .andExpect(jsonPath("$.data.ownerName").value("Test User"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @WithMockUser(username = "testuser")
  void testGetProject_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/api/projects/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Project Not Found"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testSearchProjects_ShouldReturnMatchingProjects() throws Exception {
    mockMvc.perform(get("/api/projects/search")
        .param("name", "Test"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].name").value("Test Project"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testSearchProjects_WithNoMatches_ShouldReturnEmptyList() throws Exception {
    mockMvc.perform(get("/api/projects/search")
        .param("name", "Nonexistent"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testGetUserProjects_ShouldReturnUserProjects() throws Exception {
    mockMvc.perform(get("/api/projects/my"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].name").value("Test Project"))
        .andExpect(jsonPath("$.data[0].ownerId").value(testUser.getUserId().intValue()));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testCreateProject_ShouldCreateProjectSuccessfully() throws Exception {
    CreateProjectRequest request = new CreateProjectRequest("New Project", "New Description");

    mockMvc.perform(post("/api/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("done"))
        .andExpect(jsonPath("$.data.name").value("New Project"))
        .andExpect(jsonPath("$.data.description").value("New Description"))
        .andExpect(jsonPath("$.data.ownerId").value(testUser.getUserId().intValue()))
        .andExpect(jsonPath("$.data.ownerName").value("Test User"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testCreateProject_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    CreateProjectRequest invalidRequest = new CreateProjectRequest("", "Description");

    mockMvc.perform(post("/api/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testUpdateProject_ShouldUpdateProjectSuccessfully() throws Exception {
    UpdateProjectRequest request = new UpdateProjectRequest("Updated Project", "Updated Description");

    mockMvc.perform(put("/api/projects/{id}", testProject.getProjectId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Updated Project"))
        .andExpect(jsonPath("$.data.description").value("Updated Description"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testUpdateProject_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    UpdateProjectRequest request = new UpdateProjectRequest("Updated Project", "Updated Description");

    mockMvc.perform(put("/api/projects/{id}", 999L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Project Not Found"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testDeleteProject_ShouldDeleteProjectSuccessfully() throws Exception {
    mockMvc.perform(delete("/api/projects/{id}", testProject.getProjectId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "testuser")
  void testDeleteProject_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(delete("/api/projects/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Project Not Found"));
  }

  @Test
  void testGetProject_WithoutAuthentication_ShouldBeAllowed() throws Exception {
    // Based on SecurityConfig, all endpoints are permitted
    mockMvc.perform(get("/api/projects/{id}", testProject.getProjectId()))
        .andExpect(status().isOk());
  }


  @Test
  @WithMockUser(username = "testuser")
  void testSearchProjects_WithEmptySearchTerm_ShouldReturnAllProjects() throws Exception {
    mockMvc.perform(get("/api/projects/search")
        .param("name", ""))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testSearchProjects_WithCaseInsensitiveSearch_ShouldReturnMatchingProjects() throws Exception {
    mockMvc.perform(get("/api/projects/search")
        .param("name", "TEST"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].name").value("Test Project"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void testUpdateProject_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() throws Exception {
    UpdateProjectRequest request = new UpdateProjectRequest("Only Name Updated", null);

    mockMvc.perform(put("/api/projects/{id}", testProject.getProjectId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Only Name Updated"))
        .andExpect(jsonPath("$.data.description").value("Test Description")); // Original description preserved
  }

  @Test
  @WithMockUser(username = "testuser")
  void testCreateProject_WithMinimalData_ShouldCreateProjectSuccessfully() throws Exception {
    CreateProjectRequest request = new CreateProjectRequest("Minimal Project", null);

    mockMvc.perform(post("/api/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Minimal Project"))
        .andExpect(jsonPath("$.data.description").isEmpty());
  }
}
