package com.module2.shubh.SpringBootWebTutorial.repositories;

import com.module2.shubh.SpringBootWebTutorial.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Earlier we had to define CRUD operations on a table manually using methods utilising jdbc operations
// But now we can use Hibernate which abstracts out this logic using JPA, so now we don't have to define
// our own implementation of queries, meaning we just need an interface that extends from the JPARepository
// which takes two arguments, the entity/table that we wanna do these operations on
// and the type of primary key in the table

@Repository // extends from component which makes it a bean
public interface EmployeeRepository extends JpaRepository<EmployeeEntity,Long> {

}
// ApplicationLogs: In-memory Database started
// Database JDBC URL [jdbc:h2:mem:4b5e437b-07bf-4e62-b4fb-1ab011bcc131]
// 4b5e437b-07bf-4e62-b4fb-1ab011bcc131 - session id for the connection
