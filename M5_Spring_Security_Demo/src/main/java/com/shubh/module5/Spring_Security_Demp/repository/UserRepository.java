package com.shubh.module5.Spring_Security_Demp.repository;

import com.shubh.module5.Spring_Security_Demp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}