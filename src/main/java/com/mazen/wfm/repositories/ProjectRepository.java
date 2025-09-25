package com.mazen.wfm.repositories;

import com.mazen.wfm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Find all projects owned by a user
    List<Project> findByOwner_UserId(Long owner_id);

    // Search projects by name (case-insensitive contains)
    List<Project> findByNameContainingIgnoreCase(String name);
}
