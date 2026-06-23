package com.shubh.module4.Prod_Ready_Features.repository;

import com.shubh.module4.Prod_Ready_Features.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}