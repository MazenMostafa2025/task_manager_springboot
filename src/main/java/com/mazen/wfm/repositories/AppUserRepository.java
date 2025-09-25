package com.mazen.wfm.repositories;

import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndRole(String username, UserRole role);
    long countByRole(UserRole role);
    List<AppUser> findByRole(UserRole role);
}
