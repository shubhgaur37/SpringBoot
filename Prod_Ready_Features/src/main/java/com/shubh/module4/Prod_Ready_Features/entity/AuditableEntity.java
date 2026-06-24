package com.shubh.module4.Prod_Ready_Features.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Better to have a common entity base class for audit fields to avoid adding entity listener in all entities
// then extending all entities from this class
//base class that shares its mapping information (fields, properties, and annotations) with its inheriting entity subclasses
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Audited // allows all these fields to be present in the audit_table(version) of child entities
@EntityListeners(AuditingEntityListener.class)
// Allows some annotations for auditing, populates the fields automatically
public class AuditableEntity {
    // Entity Listener Annotations
    @CreatedDate
    // enforce that this column only gets the value set once, if updatable is not set to false, then service layer
    // can manually set this field using setters. but with updatable false the query would fail
    @Column(nullable = false, updatable = false)
    LocalDateTime createdDate;

    @LastModifiedDate
    LocalDateTime updatedDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    String createdBy;

    @LastModifiedBy
    String updatedBy;
}
