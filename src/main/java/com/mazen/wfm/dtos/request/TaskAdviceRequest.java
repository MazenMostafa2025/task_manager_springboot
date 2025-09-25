package com.mazen.wfm.dtos.request;

import com.mazen.wfm.dtos.TagDTO;
import com.mazen.wfm.dtos.TaskSummaryDTO;
import com.mazen.wfm.dtos.response.UserResponse;
import com.mazen.wfm.models.Priority;
import com.mazen.wfm.models.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAdviceRequest {
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;

    private TaskSummaryDTO parentTask; // lightweight parent info
    private Set<UserResponse> assignees;
    private Set<TagDTO> tags;
}
