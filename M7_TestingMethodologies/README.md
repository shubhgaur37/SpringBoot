# M7 Testing Methodologies

> Replace the placeholders below after creating your GitHub repository and enabling GitHub Pages.

```md
[![Build](https://github.com/<username>/<repository>/actions/workflows/jacoco-report.yml/badge.svg)](https://github.com/<username>/<repository>/actions/workflows/jacoco-report.yml)
[![JaCoCo Report](https://img.shields.io/badge/JaCoCo-Live%20Report-brightgreen)](https://<username>.github.io/<repository>/)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
```


This module is a Spring Boot employee-management sample used to demonstrate
testing strategies across the service, persistence, and HTTP layers.

The application exposes a small CRUD API for employees, persists data with
Spring Data JPA, maps entities to DTOs with ModelMapper, and uses multiple test
styles to show the tradeoffs between isolated unit tests, JPA slice tests, and
full application integration tests.

## Module Purpose

The codebase is intentionally compact so the testing behavior is easy to see.
The main learning goals are:

- Writing isolated service-layer unit tests with JUnit 5 and Mockito.
- Testing Spring Data repositories with `@DataJpaTest`.
- Understanding Spring test ApplicationContext caching.
- Using Testcontainers with MySQL for database-backed tests.
- Running HTTP-level integration tests with `WebTestClient`.
- Generating code coverage with JaCoCo.
- Understanding Maven lifecycle, phases, and plugin goals.

## Technology Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- MySQL driver
- ModelMapper
- Lombok
- JUnit 5
- Mockito
- AssertJ
- H2 test database dependency
- Testcontainers MySQL
- Spring Boot WebTestClient support
- JaCoCo Maven plugin

## Application Structure

```text
src/main/java/com/Shubh/Module7/M7_TestingMethodologies
+-- M7TestingMethodologiesApplication.java
+-- config
|   +-- AppConfig.java
+-- controller
|   +-- EmployeeController.java
+-- dto
|   +-- EmployeeDTO.java
+-- entity
|   +-- Employee.java
+-- exception
|   +-- DuplicateResourceException.java
|   +-- GlobalExceptionHandler.java
|   +-- ResourceNotFoundException.java
+-- repository
|   +-- EmployeeRepository.java
+-- service
    +-- EmployeeService.java
    +-- EmployeeServiceImpl.java
```

## Domain Model

`Employee` is a JPA entity mapped to the `employees` table.

Fields:

- `id`: manually assigned primary key.
- `name`: employee name.
- `email`: unique employee email.
- `salary`: employee salary.

The entity currently does not use `@GeneratedValue`. Tests manually assign IDs,
which keeps setup simple while learning persistence testing. If ID generation is
added later, repository and integration test fixtures should stop assigning IDs
manually.

`EmployeeDTO` mirrors the entity fields and is used as the controller and service
API object.

## Runtime Configuration

The application configuration is in `src/main/resources/application.yaml`.

```yaml
spring:
  application:
    name: M7_TestingMethodologies

  datasource:
    url: jdbc:mysql://localhost:3306/SpringBoot_Test
    username: root
    password: root

  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

Runtime behavior:

- The application expects a local MySQL database named `SpringBoot_Test`.
- Hibernate recreates the schema on startup because `ddl-auto` is set to
  `create`.
- SQL logging and formatted SQL are enabled for visibility while learning.

For production-like use, replace `ddl-auto: create` with a safer setting such as
`validate` or use a migration tool such as Flyway or Liquibase.

## REST API

Base path: `/employees`

| Method | Path | Description | Success Response |
| --- | --- | --- | --- |
| `GET` | `/employees` | Fetch all employees | `200 OK` with list of `EmployeeDTO` |
| `GET` | `/employees/{employeeId}` | Fetch one employee by ID | `200 OK` with `EmployeeDTO` |
| `POST` | `/employees` | Create a new employee | `201 Created` with saved `EmployeeDTO` |
| `PUT` | `/employees/{employeeId}` | Update an existing employee | `200 OK` with updated `EmployeeDTO` |
| `DELETE` | `/employees/{employeeId}` | Delete an employee | `200 OK` with `true` |

Example request body:

```json
{
  "id": 1,
  "name": "Shubh",
  "email": "shubh@xyz.com",
  "salary": 10000.0
}
```

## Service Rules

`EmployeeServiceImpl` contains the main business rules:

- `getEmployeeById(id)` fetches an employee or throws
  `ResourceNotFoundException`.
- `getAllEmployees()` returns all employees mapped from entity to DTO.
- `createNewEmployee(inputEmployee)` rejects duplicate email addresses by
  checking `EmployeeRepository.findByEmail(...)`.
- `updateEmployeeByID(id, updateEmployee)` allows employee details to change but
  rejects email modification.
- `deleteEmployeeByID(id)` verifies that the employee exists before deleting.

## Exception Handling

`GlobalExceptionHandler` converts application exceptions into HTTP responses:

- `ResourceNotFoundException` -> `404 Not Found`
- `DuplicateResourceException` -> `400 Bad Request`
- any other `RuntimeException` -> `500 Internal Server Error`

Note: `DuplicateResourceException` is annotated with `@ResponseStatus(CONFLICT)`,
but the global handler currently maps it to `400 Bad Request`. The handler wins
for controller responses because it explicitly handles the exception.

## Testing Overview

The module demonstrates three testing levels.

| Test Type | Class | Main Purpose |
| --- | --- | --- |
| Unit test | `EmployeeServiceImplTest` | Test service logic in isolation with Mockito |
| JPA slice test | `EmployeeRepositoryTest` | Test repository behavior with a focused Spring persistence context |
| Integration test | `EmployeeControllerTestIntegrationTests` | Test real HTTP requests through controller, service, repository, and database |

## Unit Testing With Mockito

`EmployeeServiceImplTest` uses:

- `@ExtendWith(MockitoExtension.class)` to enable Mockito in JUnit 5.
- `@Mock` for `EmployeeRepository`.
- `@Spy` for `ModelMapper`, so the real mapper implementation is used.
- `@InjectMocks` for `EmployeeServiceImpl`.
- `ArgumentCaptor<Employee>` to inspect entities passed into
  `employeeRepository.save(...)`.

Covered service scenarios include:

- Fetch all employees.
- Fetch employee by valid ID.
- Create employee successfully.
- Reject duplicate email during creation.
- Reject update when employee does not exist.
- Reject email modification during update.
- Update employee details successfully.
- Reject delete when employee does not exist.
- Delete existing employee successfully.

These tests are fast because they do not start a Spring ApplicationContext and
do not connect to a real database.

## Repository Testing With `@DataJpaTest`

`EmployeeRepositoryTest` uses `@DataJpaTest` to start only the persistence slice.

Loaded components include:

- `DataSource`
- `EntityManager`
- Hibernate/JPA infrastructure
- transaction manager
- Spring Data JPA repositories
- entity mappings

Components not related to persistence, such as controllers and services, are not
loaded. This makes repository tests faster than full `@SpringBootTest` tests.

Each test runs inside a transaction that Spring rolls back after the test
finishes, keeping test data isolated.

## Testcontainers Configuration

`TestContainersConfiguration` defines a MySQL Testcontainer for tests:

```java
@Bean
@ServiceConnection
MySQLContainer<?> mySQLContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:5.7.34"))
            .withDatabaseName("employee_db")
            .withUsername("test_user")
            .withPassword("test_password");
}
```

Important behavior:

- `@TestConfiguration` keeps the container configuration out of normal
  application component scanning.
- Test classes import it explicitly with `@Import(TestContainersConfiguration.class)`.
- `@ServiceConnection` registers database connection details for Spring Boot.
- Spring Boot 3.1+ detects the service connection and uses it instead of
  replacing the `DataSource` with H2.
- The tests use a real MySQL database, which catches behavior differences that
  an in-memory database may hide.

Docker must be running before tests that use Testcontainers can execute.

## Integration Testing With `WebTestClient`

`BaseIntegrationTests` centralizes the integration test setup:

```java
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
public abstract class BaseIntegrationTestss {
    @Autowired
    protected WebTestClient webTestClient;
}
```

This configuration:

- starts the full Spring Boot application;
- starts an embedded server on a random port;
- configures `WebTestClient`;
- imports the MySQL Testcontainer;
- allows concrete integration tests to focus on endpoint behavior.

`EmployeeControllerTestIntegrationTests` verifies:

- `GET /employees`
- `GET /employees/{id}` success and not-found flows
- `POST /employees` success and duplicate-email failure
- `PUT /employees/{id}` success, not-found failure, and email-change failure
- `DELETE /employees/{id}` success and not-found failure

The test class cleans up records in `@AfterEach` with
`employeeRepository.deleteAll()` so every test starts from a predictable state.

## Spring Test Context Caching

Spring caches ApplicationContexts between test classes that use the same test
configuration. The first test class pays the cost of creating the context, while
later compatible test classes reuse it.

This matters for:

- `@DataJpaTest`
- `@SpringBootTest`
- tests importing the same Testcontainers configuration

Keeping shared integration test setup in `BaseIntegrationTests` helps reuse the
same context shape across integration tests.

## Maven Test Execution

Run unit and repository tests:

```bash
./mvnw test
```

Build the application and generate the JaCoCo report:

```bash
./mvnw clean verify
```

Run the application:

```bash
./mvnw spring-boot:run
```

Current naming caveat:

- `EmployeeControllerTestIntegrationTests` ends with `TestIT`.
- Maven Surefire's default include patterns usually run classes named
  `*Test`, `*Tests`, or `*TestCase`.
- Because of that, `EmployeeControllerTestIntegrationTests` may not run during plain
  `./mvnw test` unless Surefire is configured to include `*TestIT` or the class
  is renamed to a default pattern such as `EmployeeControllerITTest`.

This also affects JaCoCo coverage because JaCoCo can report only code executed
by tests that Maven actually runs.

## JaCoCo Configuration

JaCoCo is configured in `pom.xml` with the `jacoco-maven-plugin`.

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.15</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

What each goal does:

- `jacoco:prepare-agent` attaches the JaCoCo Java agent before tests run.
- The agent records executed bytecode into `target/jacoco.exec`.
- `jacoco:report` reads `target/jacoco.exec` and produces human-readable and
  machine-readable reports.

The report goal is bound to Maven's `verify` phase. Therefore:

- `./mvnw test` runs tests and records execution data, but may not generate the
  HTML report.
- `./mvnw package` reaches `verify`, so the report is generated.
- `./mvnw clean verify` removes stale output first, then rebuilds tests,
  coverage data, and the packaged jar.

Generated JaCoCo output:

```text
target/jacoco.exec
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
target/site/jacoco/jacoco.csv
```

Open the HTML report in a browser:

```bash
open target/site/jacoco/index.html
```

## Code Coverage

This project uses **JaCoCo** to measure code coverage during automated testing.

Generate the coverage report locally by running:

```bash
./mvnw clean verify
```

This command:

- Executes all unit, repository, and integration tests.
- Collects execution data using the JaCoCo Java agent.
- Generates HTML, XML, and CSV coverage reports.

The generated reports are available under:

```text
target/site/jacoco/
├── index.html
├── jacoco.xml
├── jacoco.csv
└── ...
```

To view the report locally, open:

```text
target/site/jacoco/index.html
```

## Continuous Integration

The project uses **GitHub Actions** to automatically:

- Build the project
- Execute all tests
- Generate the JaCoCo coverage report
- Publish the latest HTML report to GitHub Pages

### Build Status

[![Build](https://github.com/shubhgaur37/SpringBoot/actions/workflows/jacoco-report.yml/badge.svg)](https://github.com/shubhgaur37/SpringBoot/actions/workflows/jacoco-report.yml)

### Live JaCoCo Report

The latest published coverage report is available at:

https://shubhgaur37.github.io/SpringBoot/


## Maven Lifecycle Notes

Maven has three related concepts:

- Lifecycle: a complete build workflow, such as `default`, `clean`, or `site`.
- Phase: a step inside a lifecycle, such as `compile`, `test`,
  `verify`, `package`, or `install`.
- Goal: a task provided by a plugin, such as `jacoco:prepare-agent`,
  `jacoco:report`, `compiler:compile`, or `spring-boot:repackage`.

In this module:

- JaCoCo `prepare-agent` is configured as a plugin goal so coverage collection
  is available during test execution.
- JaCoCo `report` is bound to the `verify` phase.
- Maven compiler plugin executions are bound to `compile` and `test-compile`
  for Lombok annotation processing.

## Test Discovery

The project now follows Maven Surefire's default test naming conventions:

- `EmployeeServiceImplTest`
- `EmployeeRepositoryTest`
- `EmployeeControllerTestIntegrationTests`

Because the integration test class ends with `Tests`, Maven Surefire discovers it automatically. Running:

```bash
./mvnw clean verify
```

executes unit tests, repository tests, and integration tests in a single build, allowing JaCoCo to generate an accurate coverage report.

