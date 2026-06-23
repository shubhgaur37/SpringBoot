package com.shubh.module4.Prod_Ready_Features.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Better to have a common entity base class for editing to avoid adding entity listener in all entities
// then extending all entities from this class
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
// Allows some annotations for auditing, populates the fields automatically
public class AuditableEntity {
    // Entity Listener Annotations
    @CreatedDate
    @Column(nullable = false, updatable = false) // enforce that this column can only be changed once during creation
            LocalDateTime createdDate;

    @LastModifiedDate
    LocalDateTime updatedDate;

    @CreatedBy
    String createdBy;

    @LastModifiedBy
    String updatedBy;
}
