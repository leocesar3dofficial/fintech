package com.leo.fintech.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndEmail(String username, String email);

    Optional<User> findByIdAndEmail(UUID id, String email);

    Optional<User> findByIdAndUsername(UUID id, String username);

    Optional<User> findByEmail(String email);
}
