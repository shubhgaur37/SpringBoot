package com.shubh.module4.Prod_Ready_Features.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.envers.Audited;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts")
@Audited // all fields present in the audit table of this entity[created after adding @Audited to any field in the entity]
public class PostEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;
//    @NotAudited // can be used to exclude the field from the entity audit table
    String description;

    /*
    =================================================================================
    EDUCATIONAL REFERENCE: NATIVE JPA LIFECYCLE HOOKS
    =================================================================================
    The methods below are commented out because Spring Data's AuditingEntityListener
    automatically handles our creation and update timestamps behind the scenes.

    You can uncomment and implement these hooks in the future if you need to bypass
    the standard auditing listener, or if you need to trigger complex data
    manipulations during multi-entity mappings.
    =================================================================================

    @PrePersist
    void beforeSave() {
        // TRIGGERS BEFORE SQL INSERT:
        // Automatically runs right before a new record is saved to the database.
        // Use case: Setting default property values, generating custom business UUIDs,
        // or performing data normalization (e.g., lowercase email strings).
    }

    @PreUpdate
    void beforeUpdate() {
        // TRIGGERS BEFORE SQL UPDATE:
        // Automatically runs right before an existing record is updated in the database.
        // Use case: Enforcing data validation constraints or updating a completely
        // custom status field depending on changed values.
    }

    @PreRemove
    void beforeDelete() {
        // TRIGGERS BEFORE SQL DELETE:
        // Automatically runs right before a record is deleted from the database.
        // Use case: Executing complex manual cascade cleanups across multiple tables
        // or intercepting the operation to implement a custom soft-delete flag.
    }
    */
}
