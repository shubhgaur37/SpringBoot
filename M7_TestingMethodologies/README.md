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
|   +-- DataService.java
|   +-- EmployeeService.java
|   +-- impl
|       +-- DevDataService.java
|       +-- EmployeeServiceImpl.java
|       +-- ProdDataService.java

src/main/resources
+-- application.yaml
+-- application-DEV.yaml
+-- application-PROD.yaml
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

## Runtime Configuration And Spring Profiles

The base application configuration is in
`src/main/resources/application.yaml`. Spring Boot always loads this file first.
When one or more profiles are active, Spring Boot also loads matching
profile-specific files named `application-{profile}.yaml`.

```yaml
spring:
  application:
    name: M7_TestingMethodologies

  datasource:
    url: jdbc:mysql://localhost:3306/SpringBoot_Test
    username: root
    password: root

deployment:
  env: global
```

Base runtime behavior:

- The application expects a local MySQL database named `SpringBoot_Test`.
- `deployment.env` defaults to `global`.
- JPA settings are supplied by the active profile-specific configuration.

### Available Profiles

| Profile config file | Loaded by active profile | Main settings |
| --- | --- | --- |
| `application-DEV.yaml` | `DEV` | Local MySQL, `ddl-auto: create`, SQL logging enabled, formatted SQL enabled, `deployment.env: DEV` |
| `application-PROD.yaml` | `PROD` | Database URL from `PROD_DB_URL`, `ddl-auto: update`, `deployment.env: PROD` |

Spring merges profile-specific properties into the base configuration. If the
same property appears in both places, the profile-specific value wins. If a
property exists only in `application.yaml`, that base value remains active.

For example, running with the `DEV` profile loads:

```text
application.yaml
application-DEV.yaml
```

The final runtime configuration uses the base application name and datasource
defaults from `application.yaml`, then applies the DEV JPA settings and
`deployment.env: DEV` from `application-DEV.yaml`.

### Running With Profiles

Run with the DEV profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=DEV
```

Run the packaged jar with the PROD profile:

```bash
PROD_DB_URL=jdbc:mysql://prod-host:3306/SpringBoot_Test \
java -jar target/M7_TestingMethodologies-0.0.1-SNAPSHOT.jar --spring.profiles.active=PROD
```

Profiles can also be activated with the environment variable:

```bash
SPRING_PROFILES_ACTIVE=DEV ./mvnw spring-boot:run
```

### Profile-Specific Beans

The module also demonstrates profile-specific Spring beans through
`DataService`.

| Bean | Annotation | Returned environment | Returned data |
| --- | --- | --- | --- |
| `DevDataService` | `@Profile("DEV")` | `DEV_STAGING` | `DEV_STAGING_DATA` |
| `ProdDataService` | `@Profile("PROD")` | `PROD` | `PROD_DATA` |

`M7TestingMethodologiesApplication` injects `DataService` and prints the active
environment/data when the application starts.

The profile names used by the YAML files and the `@Profile` annotations now
match, so activating `DEV` creates `DevDataService` and loads
`application-DEV.yaml`; activating `PROD` creates `ProdDataService` and loads
`application-PROD.yaml`.

Note: on case-insensitive filesystems, such as the default on many macOS and
Windows installations, differently cased filenames may resolve to the same file.
That behavior comes from the operating system, not Spring Boot. On
case-sensitive filesystems, such as many Linux environments, profile file names
must match the active profile's case exactly.

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

## Flyway Migration Journey & Production Learnings

This project originally used Hibernate schema generation to evolve the database
with `spring.jpa.hibernate.ddl-auto=update`. That was useful while learning and
moving quickly, but it meant the live schema was being changed implicitly by the
ORM at startup. The project was later moved to Flyway so schema changes are
explicit, versioned, reviewed, and repeatable.

Flyway is now the intended owner of database schema evolution. Hibernate should
not create or mutate production tables. In production-like environments, the
safer target is:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

With this setup, startup follows this contract:

```text
Application starts
      |
      v
Flyway applies pending SQL migrations
      |
      v
Hibernate validates entity mappings against the final schema
      |
      v
Application becomes ready
```

This makes Flyway the single source of truth for schema history. The practical
advantages are:

- Schema changes live in version control with the application code.
- Deployments are deterministic because each environment receives the same
  ordered migration scripts.
- Development, test, staging, and production schemas stay easier to compare.
- Rollback planning and auditing become possible because schema changes are
  visible as reviewed files instead of hidden startup side effects.
- Failed deployments are easier to reason about because Flyway records exactly
  which migration version failed.

### Introducing Flyway Into An Existing Database

The production database already had application tables created by Hibernate, but
it did not have Flyway metadata. On first startup, Flyway saw a non-empty schema
without `flyway_schema_history` and failed with:

```text
Found non-empty schema but no schema history table.
```

Flyway fails here because it cannot safely infer whether existing tables came
from previous migrations, manual SQL, Hibernate, or another tool. Running `V1`
against a database that already contains those tables could duplicate objects or
damage data.

For a one-time adoption of Flyway on an existing production database, enable:

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 1
```

What this means:

- `baseline-on-migrate=true` tells Flyway to create `flyway_schema_history` for
  the existing schema instead of failing immediately.
- The current database structure is treated as the baseline.
- Migrations at or below the configured baseline version are considered already
  applied.
- With `baseline-version: 1`, migration `V1__...sql` is skipped and Flyway only
  executes `V2` and later migrations.

Warning: `baseline-on-migrate` should be used only for the initial Flyway
adoption of an existing database. After `flyway_schema_history` exists, remove
that setting from configuration. Leaving it enabled permanently weakens an
important safety check that protects against accidentally pointing the
application at the wrong non-empty schema.

### Fresh Database Vs Existing Database

Flyway behaves differently depending on whether the database already has schema
objects and history.

Fresh database, such as a new Testcontainers MySQL instance:

```text
Empty database
      |
      v
No application tables
      |
      v
Flyway executes V1 -> V2 -> V3 -> ...
      |
      v
flyway_schema_history records each successful migration
```

Existing production database during Flyway adoption:

```text
Tables already exist
      |
      v
No flyway_schema_history table
      |
      v
Use baseline-on-migrate once
      |
      v
Flyway records a baseline and runs only newer migrations
```

No baseline is required for a fresh database because Flyway can safely create
the entire schema from the first migration.

### Failed Migration Behaviour

When a migration fails, Flyway records the failed attempt in
`flyway_schema_history` with `success = false`. On the next application startup,
Flyway does not simply retry the same migration. It first validates migration
history, sees the failed entry, and stops startup.

```text
Startup
  |
  v
Flyway validate
  |
  v
Failed migration found in flyway_schema_history
  |
  v
Startup fails before normal migration execution
```

This is why fixing the SQL file alone is not enough while a failed history row
still exists.

Development recovery is usually simple:

- Inspect the failed row and local database state.
- Delete the failed row from `flyway_schema_history`.
- Fix the migration SQL.
- Restart the application so Flyway can execute it again.

Production recovery must be stricter:

- Determine exactly which SQL statements ran before the failure.
- Bring the database into the state expected by the migration version.
- Use `flyway repair` only after the real schema state matches the intended
  migration state.
- Do not blindly delete Flyway history rows in production.

### Flyway Repair

`flyway repair` repairs Flyway metadata. It does not rerun a migration and it
does not fix application tables.

Use repair only when the database has already been manually corrected to match
the expected state. Typical uses include removing failed metadata entries after
the schema has been verified, or updating metadata after an intentional repair
process. It should not be used as a way to hide an unknown failure.

### Immutable Versioned Migrations

Versioned migrations are immutable after they successfully run anywhere shared.
If this migration has already succeeded:

```text
V2__AddDepartmentColumn.sql
```

do not edit it to add another schema change. Create a new migration instead:

```text
V3__FixSomething.sql
```

Flyway stores a checksum for every successful migration. If an already executed
SQL file is edited, even for formatting or comments, Flyway calculates a new
checksum and validation fails because the file no longer matches the recorded
history.

The only practical exception is a migration that failed and is recorded with
`success = false`. In development, that file can be corrected before deleting
the failed metadata row and rerunning. In production, first inspect how far the
failed SQL got and repair the database state deliberately.

### Linux MySQL Case Sensitivity

A production failure exposed a MySQL case-sensitivity difference. AWS RDS was
running with:

```sql
lower_case_table_names = 0
```

On Linux MySQL with this setting, table names are case-sensitive:

```text
employees != EMPLOYEES
```

SQL keywords are not case-sensitive:

```sql
SELECT * FROM employees;
select * from employees;
```

Column names are generally not case-sensitive in MySQL, but relying on mixed
case still creates portability problems. The safest convention is lowercase
snake_case for tables and columns:

```text
employees
department_name
```

The production migration failed because it executed:

```sql
ALTER TABLE EMPLOYEES ...
```

while the real table name was:

```text
employees
```

The error was:

```text
Table spring_test.EMPLOYEES doesn't exist.
```

Lesson: write migration SQL using the exact object names that exist in
production, and prefer lowercase identifiers from the first migration.

### Production Deployment Debugging

One confusing production symptom was that a failed Flyway history row was
deleted manually, then immediately reappeared. The delete had worked. The real
cause was the Elastic Beanstalk application restart loop.

```text
Elastic Beanstalk starts app
      |
      v
Flyway attempts migration
      |
      v
Migration fails
      |
      v
Flyway inserts success=false
      |
      v
Application startup fails
      |
      v
Elastic Beanstalk restarts app
      |
      v
Same failed row is inserted again
```

This looked like MySQL ignored the `DELETE`, but another application startup was
recreating the failed metadata row. When debugging production migrations, check
whether the platform is continuously restarting the old or broken application
version.

### Elastic Beanstalk Rollback

Another issue was caused by the deploy stage, not the source or build stage.
CodePipeline source and CodeBuild were using the latest commit, but Elastic
Beanstalk rolled back to an older application version during deployment.

Result: the old migration SQL continued running in production even though the
latest source and build looked correct.

Important lesson:

- A successful source stage does not prove production is running that commit.
- A successful build stage does not prove the built artifact was deployed.
- When production behavior does not match the repository, inspect the deployed
  Elastic Beanstalk application version and artifact.

### Testcontainers & Flyway

For integration tests, Testcontainers starts a fresh MySQL database. Because the
database is empty, Flyway runs the full ordered migration chain:

```text
Start MySQL container
      |
      v
Create empty database
      |
      v
Flyway executes V1 -> V2 -> V2.1 -> V2.2 -> ...
      |
      v
Spring Boot starts tests
```

Flyway executes each versioned migration only once per database. If the same
database is reused later and the history table already contains successful
entries, Flyway validates the history and runs only pending newer migrations.

No baseline is needed in Testcontainers because the database does not already
contain Hibernate-created production tables.

### Matching Production Database Versions

The production database was MySQL 8.4, while the test container initially used
an older MySQL image. A migration using:

```sql
ALTER TABLE employees
    RENAME COLUMN old_name TO new_name;
```

worked in production but failed in tests because the test database did not match
the production database version closely enough.

Best practice: use the same MySQL major version in Testcontainers as production.
For this project, that means using a MySQL 8.x image when production is MySQL
8.4, rather than an older 5.7 image.

### Build & Deployment Learnings

The repository contains multiple Maven projects. CodeBuild checks out the
repository contents and starts from the repository root, so `buildspec.yml`
must move into the Spring Boot module before running Maven:

```yaml
phases:
  build:
    commands:
      - cd M7_TestingMethodologies
      - mvn clean package
```

The Maven command runs relative to the current working directory. Artifact
packaging is separate. Because the JAR is created under the module's `target`
directory, the artifact section should point there:

```yaml
artifacts:
  base-directory: M7_TestingMethodologies/target
  files:
    - "*.jar"
  discard-paths: yes
```

`discard-paths` only controls the layout of the final packaged build artifact
uploaded by CodeBuild. It does not change where Maven runs, how Maven resolves
the project, where Maven writes `target/`, or how CodeBuild discovers files
before `base-directory` and `files` are applied.

### CI/CD Path Learnings

CodeBuild clones the contents of the Git repository into its workspace. It does
not create an extra parent directory named after the repository.

```text
Local:
SpringBoot/
  M7_TestingMethodologies/
  OtherModule/

CodeBuild workspace:
M7_TestingMethodologies/
OtherModule/
```

Therefore all paths in `buildspec.yml` are relative to the repository root
contents, not to the local parent directory. Assuming the repository folder
itself existed inside CodeBuild caused the initial path-related build failures.

### Final Flyway And Deployment Best Practices

- Let Flyway own schema evolution.
- Use Hibernate `ddl-auto=validate` for production-like environments.
- Keep versioned migrations immutable after they succeed.
- Create a new migration for every new schema change.
- Use `baseline-on-migrate` only once when adopting Flyway for an existing
  database.
- Remove `baseline-on-migrate` after Flyway history exists.
- Understand Flyway validation before assuming a fixed SQL file will rerun.
- Do not use `flyway repair` until the real database state has been inspected
  and corrected.
- Match the Testcontainers database major version to production.
- Use lowercase table and column names consistently.
- Inspect the deployed Elastic Beanstalk application version when production
  behavior differs from the latest commit.
- Treat migration files as production application code: review them carefully,
  version them deliberately, and test them against a production-like database.


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
