package com.mazen.wfm.repositories;

import com.mazen.wfm.models.Status;
import com.mazen.wfm.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks by project
    List<Task> findByProject_ProjectId(Long projectId);
    Page<Task> findByProject_ProjectId(Long projectId, Pageable pageable);

    // Find all tasks assigned to a specific user
    List<Task> findByAssignees_UserId(Long userId);

    // Find tasks by status
    List<Task> findByStatus(Status status);

    // Find tasks due before a given date
    List<Task> findByDueDateBefore(java.time.LocalDate date);

    // Find tasks due before a given date
    List<Task> findByDueDateBetweenAndStatusIn(LocalDate start, LocalDate end, List<Status> statuses);

    // Find tasks by project id and the task status
    List<Task> findByProject_ProjectIdAndStatus(Long projectId, Status status);

    @Query("SELECT t FROM Task t JOIN t.assignees u " +
            "WHERE u.userId = :userId AND t.status IN :statuses")
    List<Task> findTasksByUserIdAndStatuses(@Param("userId") Long userId,
                                            @Param("statuses") List<Status> statuses);
}
