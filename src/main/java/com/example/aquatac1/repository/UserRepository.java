package com.example.aquatac1.repository;

import com.example.aquatac1.model.ERole;
import com.example.aquatac1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findByRoles_Name(ERole role);
    List<User> findByIsApprovedFalse();

}