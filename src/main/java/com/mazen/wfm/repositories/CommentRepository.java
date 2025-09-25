package com.mazen.wfm.repositories;

import com.mazen.wfm.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find all comments on a task
    List<Comment> findByTask_TaskId(Long taskId);

    // Find all comments made by a user
    List<Comment> findByAuthor_UserId(Long userId);
}