package com.shubh.JPATutorial.Module3.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
// generates required args constructor for final and non-null fields
//if not present, generates a NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Entity
@NoArgsConstructor
// used to define properties and constraints of table
@Table(name = "product_table",
        uniqueConstraints = { // define unique constraints for columns in the tab;e
//                @UniqueConstraint(name = "sku_unique", columnNames = {"sku"}),
                // index is created whenever a unique_constraint is defined,ensure the column names
                // match exactly if explicity defined in @Column annotation
                // column names are automatically inferred from field names(from object)
                @UniqueConstraint(name = "title_price_unique", columnNames = {"title_x", "priceCurrent"})
        },
        // create index on a column (wasteful) as unique constraint already created an index
        // if name of the index is not provided, it is auto-generated
        indexes = {@Index(name = "skuIndex", columnList = "sku")}
)
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    //    define constraints on columns
    @Column(nullable = false, length = 20)
    String sku;
    @Column(name = "title_x")
    String title;

    BigDecimal priceCurrent;

    Integer quantity;

    //    Handle timestamp initialization at the DB level, offload from application code to DB
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
//  camel-case gets converted to snake-case in tables
    LocalDateTime updatedAt;
}
