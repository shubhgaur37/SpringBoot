# M7 Testing Methodologies

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
| Integration test | `EmployeeControllerTestIT` | Test real HTTP requests through controller, service, repository, and database |

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

`BaseIntegrationTest` centralizes the integration test setup:

```java
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
public abstract class BaseIntegrationTest {
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

`EmployeeControllerTestIT` verifies:

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

Keeping shared integration test setup in `BaseIntegrationTest` helps reuse the
same context shape across integration tests.

## Maven Test Execution

Run unit and repository tests:

```bash
./mvnw test
```

Build the application and generate the JaCoCo report:

```bash
./mvnw clean package
```

Run the application:

```bash
./mvnw spring-boot:run
```

Current naming caveat:

- `EmployeeControllerTestIT` ends with `TestIT`.
- Maven Surefire's default include patterns usually run classes named
  `*Test`, `*Tests`, or `*TestCase`.
- Because of that, `EmployeeControllerTestIT` may not run during plain
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
            <phase>prepare-package</phase>
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

The report goal is bound to Maven's `prepare-package` phase. Therefore:

- `./mvnw test` runs tests and records execution data, but may not generate the
  HTML report.
- `./mvnw package` reaches `prepare-package`, so the report is generated.
- `./mvnw clean package` removes stale output first, then rebuilds tests,
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

## Current Coverage Snapshot

The generated report currently shows:

- Overall instruction coverage: 75%.
- Overall branch coverage: 100%.
- `EmployeeServiceImpl`: 100% instruction, branch, line, method, and class
  coverage.
- `EmployeeController`: 0% in the current report.
- `AppConfig`: 0% in the current report.
- `GlobalExceptionHandler`: 0% in the current report.

The controller has integration tests, so the 0% controller coverage is most
likely caused by the integration test class not matching Maven Surefire's
default test naming patterns.

## Maven Lifecycle Notes

Maven has three related concepts:

- Lifecycle: a complete build workflow, such as `default`, `clean`, or `site`.
- Phase: a step inside a lifecycle, such as `compile`, `test`,
  `prepare-package`, `package`, or `install`.
- Goal: a task provided by a plugin, such as `jacoco:prepare-agent`,
  `jacoco:report`, `compiler:compile`, or `spring-boot:repackage`.

In this module:

- JaCoCo `prepare-agent` is configured as a plugin goal so coverage collection
  is available during test execution.
- JaCoCo `report` is bound to the `prepare-package` phase.
- Maven compiler plugin executions are bound to `compile` and `test-compile`
  for Lombok annotation processing.

## Recommended Coverage Improvement

To include integration tests in normal Maven test execution, use one of these
approaches.

Option 1: rename the class:

```text
EmployeeControllerTestIT.java -> EmployeeControllerITTest.java
```

Option 2: configure Surefire to include `*TestIT`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
            <include>**/*TestCase.java</include>
            <include>**/*TestIT.java</include>
        </includes>
    </configuration>
</plugin>
```

Option 3: split integration tests into Failsafe:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

With Failsafe, integration tests commonly use names such as `*IT.java`, and the
usual command is:

```bash
./mvnw verify
```

For this module's current learning-oriented setup, renaming the class or adding
a Surefire include is the smallest change.

## Git Comment History Summary

Recent commit messages show the module evolving in this order:

- Repository tests were added first for persistence-layer coverage.
- Comments were added about test database replacement and Spring Boot test
  performance.
- `@DataJpaTest` behavior and Spring ApplicationContext caching were documented
  in code comments.
- Testcontainers dependencies and MySQL container configuration were added.
- Service-layer unit tests were expanded to reach full service coverage.
- WebTestClient dependencies and controller integration tests were added.
- A global exception handler was added for integration-test HTTP responses.
- A base integration test class was introduced to centralize common setup.
- JaCoCo was added, with Maven lifecycle, phase, and goal comments in the POM.

The comments in the code are mainly educational. They explain why each testing
annotation or dependency exists, what Spring Boot is doing behind the scenes, and
which tradeoffs each test style makes.

## Practical Notes

- Keep unit tests focused on service logic and repository interactions.
- Use repository slice tests when validating JPA queries, mappings, and database
  constraints.
- Use integration tests for full HTTP flows and exception-to-response behavior.
- Keep Testcontainers tests dependent on Docker availability.
- Regenerate JaCoCo reports with `./mvnw clean package` after changing tests.
- Check `target/site/jacoco/index.html` before trusting a coverage percentage;
  stale reports or skipped integration tests can give misleading results.
