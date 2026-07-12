package com.shubh.module5.Spring_Security_Demo.repository;

import com.shubh.module5.Spring_Security_Demo.entity.PostEntity;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    @Query("SELECT COUNT(*) FROM PostEntity p WHERE p.author = :user ")
    int getPostsCount(@Param("user") UserEntity user);
}