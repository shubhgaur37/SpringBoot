package com.shubh.module4.Prod_Ready_Features.controller;

import com.shubh.module4.Prod_Ready_Features.entity.PostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/audits")
public class AuditController {

    /*
     * We inject the EntityManagerFactory instead of a shared EntityManager bean.
     * Shared EntityManagers inside controllers usually require an active transaction.
     * Because this is a read-only audit endpoint, we do not want or need to start
     * a global transactional context.
     */
    @Autowired
    EntityManagerFactory entityManagerFactory;

    // Entity can be used here instead of DTO as this is an admin API
    @GetMapping(path = "/posts/{postId}")
    List<PostEntity> getPostRevisions(@PathVariable Long postId) {
        /*
         * WHY AN AD-HOC ENTITY MANAGER IS PREFERRED HERE:
         *
         * 1. Low Overhead: Creating an EntityManager from an already initialized
         *    factory is highly lightweight. It simply borrows a connection from the pool.
         *
         * 2. No Transaction Needed for Reads: Envers read operations are purely database
         *    queries. They do not require a transactional context to read historical data.
         *
         * 3. Prevents First-Level Cache Pollution: A shared EntityManager caches every
         *    entity instance it reads. If we queried 10 historical versions of the same
         *    PostEntity (same ID, different states), they would conflict inside a shared
         *    persistence context. An ad-hoc manager isolates this completely.
         *
         * 4. Separation of Concerns: Audit logs are queried infrequently. Keeping this traffic
         *    isolated prevents slow audit queries from interfering with live business data.
         *
         *
         * WHY THE 'try-with-resources' STATEMENT IS REQUIRED:
         *
         * 1. Automatic Resource Management: The EntityManager object opens a direct, active
         *    connection to your database. Because we spawned it manually via the factory,
         *    Spring's automatic lifecycle container will NOT manage or close it for us.
         *
         * 2. Preventing Connection Leaks: If we omit the close step, the connection remains
         *    trapped and active. Repeated requests to this endpoint will exhaust the
         *    application's connection pool (HikariCP), eventually causing the entire
         *    application to freeze and crash with connection timeout errors.
         *
         * 3. Safe Exception Handling: Since EntityManager implements the 'AutoCloseable'
         *    interface, declaring it inside 'try (...)' guarantees that Java will automatically
         *    invoke 'entityManager.close()' when execution exits this block. This happens
         *    even if a database exception occurs mid-execution, preventing unclosed resources.
         */
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            // AuditReader wraps the isolated session to query historical states
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            // Fetch a list of revision IDs where changes occurred for this specific post
            List<Number> revisions = auditReader.getRevisions(PostEntity.class, postId);

            // Map each revision number to the actual entity snapshot at that point in time
            return revisions
                    .stream()
                    .map(revisionNumber -> auditReader.find(PostEntity.class, postId, revisionNumber))
                    .toList();
        }
    }
}
