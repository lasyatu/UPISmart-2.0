package com.upismart.backend.repository;

import com.upismart.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUpiHandle(String upiHandle);
    Optional<UserEntity> findByEmail(String email);
}
