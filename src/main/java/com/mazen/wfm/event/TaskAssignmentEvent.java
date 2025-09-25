package com.mazen.wfm.event;

import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TaskAssignmentEvent {
    private Long taskId;
    private String taskName;
    private String taskDescription;
    private Long userId;
    private String userEmail;
    private String userName;
    private LocalDateTime assignedAt;

    public TaskAssignmentEvent(Task task, AppUser user) {
        this.taskId = task.getTaskId();
        this.taskName = task.getTitle();
        this.taskDescription = task.getDescription();
        this.userId = user.getUserId();
        this.userEmail = user.getEmail();
        this.userName = user.getUsername();
        this.assignedAt = LocalDateTime.now();
    }
}
