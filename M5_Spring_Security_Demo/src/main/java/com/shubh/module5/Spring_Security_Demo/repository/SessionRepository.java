package com.shubh.module5.Spring_Security_Demo.repository;

import com.shubh.module5.Spring_Security_Demo.entity.Session;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUser(UserEntity user);

    Optional<Session> findByRefreshToken(String refreshToken);
}