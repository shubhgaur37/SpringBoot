package com.shubh.module5.Spring_Security_Demp.repository;

import com.shubh.module5.Spring_Security_Demp.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}