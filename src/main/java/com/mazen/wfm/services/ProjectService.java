package com.mazen.wfm.services;

import com.mazen.wfm.dtos.request.CreateProjectRequest;
import com.mazen.wfm.dtos.request.UpdateProjectRequest;
import com.mazen.wfm.dtos.response.ProjectResponse;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.ProjectMapper;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Project;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import com.mazen.wfm.repositories.TaskRepository;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
//    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, AppUserRepository appUserRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
//        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.projectMapper = projectMapper;
    }

    public ProjectResponse createProject(CreateProjectRequest request, String username) {
        AppUser owner = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        Project project = projectMapper.toEntity(request);
        project.setOwner(owner);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project Not Found"));
    }

    public List<Project> getProjectsByUserName(String username) {
        AppUser user = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return projectRepository.findByOwner_UserId(user.getUserId());
    }

    public List<Project> searchProjectsByName(String keyword) {
        return projectRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Project updateProject(long id, UpdateProjectRequest request) {
        Project project = this.getProjectById(id);
        projectMapper.updateEntityFromRequest(request, project);
        return projectRepository.save(project);
    }

    public void deleteProject(String username, Long projectId) {
        AppUser currUser = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        Project project = this.getProjectById(projectId);
        if (!currUser.getUserId().equals(project.getOwner().getUserId()) && currUser.getRole() != UserRole.ADMIN)  {
            throw new AuthorizationDeniedException("You are not the owner of the project");
        }
        projectRepository.deleteById(projectId);
    }
}
