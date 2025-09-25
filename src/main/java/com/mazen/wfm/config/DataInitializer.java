package com.mazen.wfm.config;

import com.mazen.wfm.models.*;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.repositories.ProjectRepository;
import com.mazen.wfm.repositories.TagRepository;
import com.mazen.wfm.repositories.TaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test") // Don't run in tests
public class DataInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;

    @PostConstruct
    public void init() {
        // Create initial admin user if no users exist
//        if (appUserRepository.count() == 0) {
            log.info("No users found, creating initial admin user");
            
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFullName("System Administrator");
            admin.setEmail("admin@example.com");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            appUserRepository.save(admin);
            AppUser mazen = new AppUser();
            mazen.setUsername("mazen");
            mazen.setPassword(passwordEncoder.encode("mazen"));
            mazen.setFullName("mazen");
            mazen.setEmail("mazen@example.com");
            mazen.setRole(UserRole.USER);
            mazen.setActive(true);
            appUserRepository.save(mazen);
            log.info("Created initial user with username: mazen and password: mazen");
            for (int i = 1 ; i <= 4; i++) {
                Project proj = new Project();
                proj.setName("proj " + i + " mazen");
                proj.setOwner(mazen);
                proj.setDescription("proj " + i + " mazen");
                projectRepository.save(proj);
                Project proj2 = new Project();
                proj2.setName("proj " + i + " admin");
                proj2.setOwner(admin);
                proj2.setDescription("proj " + i + " admin");
                projectRepository.save(proj2);
            }
        LocalDate weekAhead = LocalDate.now().plusWeeks(1);
        for (Status s : Status.values()) {
            Task task = new Task();
            task.setTitle("task project 1 with varying status");
            task.setPriority(Priority.LOW);
            task.setProject(projectRepository.findById(1L).orElseThrow());
            task.setDescription("task project 1 with varying status");
            task.setDueDate(weekAhead);
            task.setStatus(s);
            taskRepository.save(task);
            Task task2 = new Task();
            task2.setPriority(Priority.LOW);
            task2.setTitle("task project 5 with varying status");
            task2.setProject(projectRepository.findById(5L).orElseThrow());
            task2.setStatus(s);
            task2.setDescription("task project 5  with varying status");
            task2.setDueDate(weekAhead.plusDays(1));
            taskRepository.save(task2);
        }
        for (Priority p : Priority.values()) {
            Task task = new Task();
            task.setTitle("task project 1 with varying priority");
            task.setPriority(p);
            task.setProject(projectRepository.findById(1L).orElseThrow());
            task.setDescription("task project 1 with varying priority");
            task.setStatus(Status.TODO);
            task.setDueDate(weekAhead);
            taskRepository.save(task);
            Task task2 = new Task();
            task2.setPriority(p);
            task2.setStatus(Status.TODO);
            task2.setTitle("task project 5 with varying priority");
            task2.setProject(projectRepository.findById(5L).orElseThrow());
            task2.setDescription("task project 5 with varying priority");
            task2.setDueDate(weekAhead.plusDays(1));
            taskRepository.save(task2);
        }
        for(int i = 0 ; i < 10; i++) {
            Tag t = new Tag();
            t.setName("tag " + (i+1));
            tagRepository.save(t);
        }

    }
}
