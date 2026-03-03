package com.module2.shubh.SpringBootWebTutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
// Earlier we had to define CRUD operations on a table manually using methods utilising jdbc operations
// But now we can use Hibernate which abstracts out this logic using JPA, so now we don't have to define
// our own implementation of queries, meaning we just need an interface that extends from the jpa
public class EmployeeRepository {
}
