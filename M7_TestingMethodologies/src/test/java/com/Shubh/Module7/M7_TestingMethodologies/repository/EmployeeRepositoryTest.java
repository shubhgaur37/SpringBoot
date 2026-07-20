package com.Shubh.Module7.M7_TestingMethodologies.repository;

import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Starts the complete Spring Boot application context, including all
// auto-configured beans (controllers, services, repositories, configuration,
// etc.). Since the entire application is initialized, these are integration
// tests and are slower than slice tests such as @DataJpaTest.
@SpringBootTest
// Replaces the application's configured DataSource with an embedded test
// database. Spring Boot scans the test classpath for a supported embedded
// database (H2, HSQLDB, or Derby). Since H2 is present, an in-memory H2
// database is automatically configured instead of the application's MySQL
// database.
//
// Benefits:
//   • Tests do not modify the real database.
//   • Faster execution because the database runs in-memory.
//   • Each test run starts with a clean, isolated database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EmployeeRepositoryTest {

    // Injects the EmployeeRepository bean from the Spring application context.
    @Autowired
    private EmployeeRepository employeeRepository;

    // Executed before every @Test method to populate the database with a
    // consistent dataset so each test starts from a known state.
    @BeforeEach
    void saveEmployeeInRepository() {

        // Test data setup.
        //
        // Note:
        // If Employee used @GeneratedValue, manually assigning an ID would make
        // JPA treat the entity as an existing (detached) entity. Consequently,
        // save() would invoke merge() instead of persist(), potentially causing
        // an ObjectOptimisticLockingFailureException if no matching row exists.
        //
        // The ID is assigned manually here only because @GeneratedValue has
        // been removed for simplicity while learning repository testing.
        Employee employee = new Employee(
                1L,
                "Shubh",
                "shubh@xyz.com",
                10000D
        );

        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("Test: Employee details returned when querying by a valid email")
    void testFindByEmail_WhenValidEmail_ThenReturnEmployee() {

        String email = "shubh@xyz.com";

        // Execute the repository query.
        List<Employee> employeeList = employeeRepository.findByEmail(email);

        // Verify the repository returns the expected employee.
        assertThat(employeeList).isNotNull();
        assertThat(employeeList).isNotEmpty();
    }

    @Test
    @DisplayName("Test: Empty employee list returned when repository queried with an invalid email")
    void testFindByEmail_WhenInvalidEmail_ThenReturnEmployeeListEmpty() {

        String email = "afv@xyz.com";

        // Execute the repository query.
        List<Employee> employeeList = employeeRepository.findByEmail(email);

        // Verify that no employee matches the supplied email.
        assertThat(employeeList).isNotNull();
        assertThat(employeeList).isEmpty();
    }
}


// Test classpath:
//
// Maven/Gradle create a separate classpath for running tests. It contains:
//   • All application classes and dependencies.
//   • Test classes and resources.
//   • Dependencies declared with <scope>test</scope>.
//
// Benefits:
//   • Test-only libraries (JUnit, AssertJ, Mockito, H2, etc.) are available
//     during testing but are excluded from the production application.
//   • Production dependencies remain smaller and cleaner.
//   • Tests can safely use lightweight embedded databases and mocking
//     libraries without affecting the deployed application.
//
// Since H2 is declared with test scope in this project, it is available only
// while executing tests.