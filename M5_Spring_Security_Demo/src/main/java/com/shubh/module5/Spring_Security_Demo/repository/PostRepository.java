package com.shubh.module5.Spring_Security_Demo.repository;

import com.shubh.module5.Spring_Security_Demo.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}