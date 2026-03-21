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
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

}
// ApplicationLogs: File Database started
// Added connection conn0: url=jdbc:h2:file:/Users/shubhgaur/Documents/SpringBoot/Module2/tutorialDB/db user="SHUBHDB"