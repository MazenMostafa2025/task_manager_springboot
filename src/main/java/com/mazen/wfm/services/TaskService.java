package com.mazen.wfm.services;

//import com.mazen.wfm.config.RabbitMQConfig;
import com.mazen.wfm.dtos.request.TaskRequest;
import com.mazen.wfm.event.TaskAssignmentEvent;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.mapper.TaskMapper;
import com.mazen.wfm.models.*;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import com.mazen.wfm.repositories.TagRepository;
import com.mazen.wfm.repositories.TaskRepository;

//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
//    private final RabbitTemplate rabbitTemplate;
//    private final EmailService emailService;


    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, ProjectRepository projectRepository, AppUserRepository appUserRepository, TagRepository tagRepository
//            , RabbitTemplate rabbitTemplate
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.tagRepository = tagRepository;
//        this.rabbitTemplate = rabbitTemplate;
//        this.emailService = emailService;
    }

    public Task createTask(Task task) {
        Project project = projectRepository.findById(task.getProject().getProjectId()).orElseThrow(() -> new ResourceNotFoundException("Project Not Found"));
        task.setProject(project);
        return taskRepository.save(task);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("No task with this id"));
    }

    public Page<Task> getTasksByProject(Long projectId, Pageable pageable) {
        return taskRepository.findByProject_ProjectId(projectId, pageable);
    }

    public List<Task> getTasksByProjectAndStatus(Long projectId, Status status) {
        return taskRepository.findByProject_ProjectIdAndStatus(projectId, status);
    }

    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignees_UserId(userId);
    }
    public List<Task> getTasksByUserAndStatus(Long userId, List<Status> statuses) {
        return taskRepository.findTasksByUserIdAndStatuses(userId, statuses);
    }

    public List<Task> getTasksByStatus(Status status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getOverdueTasks(LocalDate today) {
        return taskRepository.findByDueDateBefore(today);
    }

    public Task updateTask(long id, TaskRequest request) {
        Task existingTask = this.getTaskById(id);
        taskMapper.updateEntityFromRequest(request, existingTask);
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Task assignUsersToTask(Long taskId, Set<Long> userIds) throws ResponseStatusException {
        Task task = this.getTaskById(taskId);
        Set<AppUser> users = new HashSet<>(appUserRepository.findAllById(userIds));

        if (users.isEmpty())
            throw new ResourceNotFoundException("No valid users found for given IDs");

        if (task.getAssignees() == null)
            task.setAssignees(new HashSet<>());

        Set<AppUser> existing = task.getAssignees();
        Set<AppUser> newUsers = users.stream()
                .filter(u -> !existing.contains(u))
                .collect(Collectors.toSet());

        existing.addAll(newUsers);
        taskRepository.save(task);
//        newUsers.forEach(user -> {
//            TaskAssignmentEvent event = new TaskAssignmentEvent(task, user);
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.TASK_ASSIGNMENT_EXCHANGE,
//                    "task.assigned.email",
//                    event
//            );
//
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.TASK_ASSIGNMENT_EXCHANGE,
//                    "task.assigned.websocket",
//                    event
//            );
//        });
        return task;
    }

    public Task assignTags(Set<Long> tagIds, Long taskId) {
        Task task = this.getTaskById(taskId);
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.isEmpty())
            throw new ResourceNotFoundException("No valid tags found for given IDs");

        if (task.getTags() == null)
            task.setTags(new HashSet<>());

        task.getTags().addAll(tags);
        return taskRepository.save(task);
    }

    public void clearTags (Long taskId) {
        Task task = this.getTaskById(taskId);
        task.getTags().clear();
        taskRepository.save(task);
    }

    public void clearAssignees (Long taskId) {
        Task task = this.getTaskById(taskId);
        task.getAssignees().clear();
        taskRepository.save(task);
    }

    public void sendReminders(LocalDate now, LocalDate upcoming) {
        List<Task> tasks = taskRepository.findByDueDateBetweenAndStatusIn(now, upcoming, List.of(Status.TODO, Status.IN_PROGRESS));

        for (Task task : tasks) {
            for (AppUser user : task.getAssignees()) {
//                emailService.sendEmail(
//                        user.getEmail(),
//                        "Reminder: Task due soon",
//                        "Your task '" + task.getTitle() + "' is due on " + task.getDueDate()
//                );
            }
        }
    }
}
