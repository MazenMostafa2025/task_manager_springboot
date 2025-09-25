package com.mazen.wfm.repositories;

import com.mazen.wfm.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // Find tag by name
    Optional<Tag> findByName(String name);
}
