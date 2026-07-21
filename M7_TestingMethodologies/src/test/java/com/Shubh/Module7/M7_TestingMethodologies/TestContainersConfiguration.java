package com.Shubh.Module7.M7_TestingMethodologies;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

// Defines additional beans that are loaded only for tests.
// Unlike @Configuration, this class is not automatically detected during
// component scanning and must be explicitly imported into test classes.
@TestConfiguration
public class TestContainersConfiguration {

    // In-memory databases (such as H2) are fast and convenient for testing,
    // but they may not fully replicate the behavior of the production
    // database. Running tests against a real MySQL instance provides greater
    // confidence by validating SQL dialects, transactions, constraints,
    // indexing, and other database-specific features.
    //
    // Testcontainers automatically:
    //   • Pulls the Docker image if it is not available locally.
    //   • Starts the container before the tests execute.
    //   • Stops and removes the container after the tests complete.
    @Bean

    // Marks this TestContainer as the application's primary database connection.
    //
    // Behind the scenes, `@ServiceConnection` registers a `JdbcConnectionDetails`
    // bean in the Spring context. Spring Boot 3.1+ detects this explicit
    // configuration and automatically skips the default behavior of replacing
    // the DataSource with an in-memory database (like H2).
    //
    /**-----------IMP----------*/
    // This allows `@DataJpaTest` to connect directly to the MySQL container
    // using just an `@Import` statement, without needing `@AutoConfigureTestDatabase`.
    @ServiceConnection
    MySQLContainer<?> mySQLContainer() {

        // MySQLContainer is a generic class. The <?> wildcard indicates that we
        // don't need to specify its exact generic type here.
        // MySQLContainer uses generics internally to support
        // fluent method chaining, but we do not need to reference the type.
        return new MySQLContainer<>(
                DockerImageName.parse("mysql:5.7.34") // docker image name from docker repository
        )
                // Creates a database named 'employee_db' inside the container.
                .withDatabaseName("employee_db")

                // Creates the default database user.
                .withUsername("test_user")
                .withPassword("test_password")

                // Assigns a fixed Docker container name.
                // Useful for debugging, but random names are generally
                // preferred in automated test suites to avoid conflicts.
                .withCreateContainerCmdModifier(cmd ->
                        cmd.withName("employee-mysql-test"));
    }
}