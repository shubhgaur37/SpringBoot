package com.Shubh.Module7.M7_TestingMethodologies.repository;

import com.Shubh.Module7.M7_TestingMethodologies.TestContainersConfiguration;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Configures a JPA test slice instead of starting the entire application.
//
// Spring Boot loads only the persistence layer, including:
//   • DataSource
//   • EntityManager / Persistence Context
//   • Hibernate (JPA provider)
//   • Transaction Manager
//   • Spring Data JPA repositories
//   • Entity classes and JPA-related configuration

// Spring caches the ApplicationContext between test classes that use the
// same configuration. The first test class incurs the cost of creating the
// context, while subsequent tests reuse the cached context, making them
// start much faster.
// This behavior applies to both @DataJpaTest and @SpringBootTest.

// Components unrelated to persistence (controllers, services, security,
// schedulers, etc.) are not loaded, making repository tests significantly
// faster than @SpringBootTest.
//
// Each test runs inside a transaction that is automatically rolled back after
// completion, ensuring every test starts with a clean database state.
//
// By default, if an embedded database (H2, HSQLDB, or Derby) is available on
// the test classpath, Spring Boot replaces the application's configured
// DataSource with that embedded database.
@DataJpaTest
@Import(TestContainersConfiguration.class)
// Uncomment to disable DataSource replacement and use the application's
// configured database instead (for example, MySQL or PostgreSQL).
//
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    // Injects the Spring Data JPA repository bean created as part of the
    // JPA test slice.
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
        assertThat(employeeList.get(0).getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Test: Empty employee list returned when repository queried with an invalid email")
    void testFindByEmail_WhenInvalidEmail_ThenReturnEmployeeListEmpty() {
        // Arrange: Given
        String email = "afv@xyz.com";

        // Act: When
        // Execute the repository query.
        List<Employee> employeeList = employeeRepository.findByEmail(email);

        // Assert: Then
        // Verify that no employee matches the supplied email.
        assertThat(employeeList).isNotNull();
        assertThat(employeeList).isEmpty();
    }
}

/*
 * Test Classpath
 * ---------------------------------------------------------------------------
 * Maven/Gradle create a separate classpath for running tests. It contains:
 *   • All application classes and dependencies.
 *   • Test classes and resources.
 *   • Dependencies declared with <scope>test</scope>.
 *
 * Benefits:
 *   • Test-only libraries (JUnit, AssertJ, Mockito, H2, etc.) are available
 *     only during testing and are excluded from the production application.
 *   • Production artifacts remain smaller and free of testing dependencies.
 *   • Tests can safely use lightweight embedded databases and mocking
 *     libraries without affecting the deployed application.
 *
 * Since H2 is declared with test scope in this project, it is available only
 * while executing tests.
 */