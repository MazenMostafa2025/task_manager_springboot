package com.mazen.wfm.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type;   // e.g. "TASK_ASSIGNED", "COMMENT_ADDED"
    private String message;
    private Long taskId;
    private Long userId;
    private LocalDate timestamp;

    // getters/setters/constructors
}
