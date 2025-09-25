package com.mazen.wfm.scheduler;

import com.mazen.wfm.services.TaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReminderScheduler {

    private final TaskService taskService;

    public ReminderScheduler(TaskService taskService) {
        this.taskService = taskService;
    }

    // Run every day
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void sendTaskReminders() {
        LocalDate now = LocalDate.now();
        LocalDate upcoming = now.plusDays(3); // tasks due in next 3 days
        taskService.sendReminders(now, upcoming);
    }
}
