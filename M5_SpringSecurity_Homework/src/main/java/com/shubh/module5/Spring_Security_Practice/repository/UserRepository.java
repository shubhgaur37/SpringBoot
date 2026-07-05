package com.shubh.module5.Spring_Security_Practice.repository;

import com.shubh.module5.Spring_Security_Practice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}