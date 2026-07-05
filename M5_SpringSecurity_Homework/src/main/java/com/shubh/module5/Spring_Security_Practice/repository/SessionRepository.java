package com.shubh.module5.Spring_Security_Practice.repository;

import com.shubh.module5.Spring_Security_Practice.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}