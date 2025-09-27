package com.mazen.wfm.dtos.response;

import com.mazen.wfm.dtos.TagDTO;
import com.mazen.wfm.dtos.TaskSummaryDTO;
import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long taskId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long projectId;
    private String projectName;
    private TaskSummaryDTO parentTask; // lightweight parent info
    private Set<UserResponse> assignees;
    private Set<TagDTO> tags;
}


