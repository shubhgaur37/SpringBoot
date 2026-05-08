# College Management Project Documentation

## 1. What this project is

This is a Spring Boot 4 REST API for managing a small college domain with four core concepts:

- `Professor`
- `Subject`
- `Student`
- `AdmissionRecord`

The project is designed around JPA entity relationships, DTO-based API responses, and service-layer relationship management.

## 2. Tech stack

- Java `21`
- Spring Boot `4.0.5`
- Spring Web MVC
- Spring Data JPA
- MySQL
- ModelMapper
- Jakarta Validation
- Lombok
- JUnit / Spring Boot Test

## 3. Current runtime setup

Configuration is in [application.yaml](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/resources/application.yaml:1).

- Database: MySQL
- URL: `jdbc:mysql://localhost:3306/College`
- Username: `YOUR_DB_USERNAME`
- Password: `YOUR_DB_PASS`
- Hibernate DDL: `create`
- SQL logging: enabled

Important implications:

- The schema is recreated on startup because `spring.jpa.hibernate.ddl-auto=create`.
- The app and tests currently expect a local MySQL instance.
- Test execution currently fails if MySQL is not reachable.

## 4. Domain model

### Professor

Defined in [Professor.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Professor.java:1).

- Has `id`, `name`
- Has `OneToMany` relationship with `Subject`
- Uses `cascade = CascadeType.ALL`
- Uses `orphanRemoval = true`

Meaning:

- A professor can teach multiple subjects.
- A subject cannot exist without a professor in this design.
- Deleting a professor deletes their subjects.

### Subject

Defined in [Subject.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Subject.java:1).

- Has `id`, `title`
- `title` is unique
- Owns `ManyToOne` link to `Professor` through `professor_id`
- Owns `ManyToMany` link to `Student` through `subject_student_mapping`

Meaning:

- Subject is the bridge between professors and students.
- The professor link is mandatory.
- Student enrollment is optional and can happen later.

### Student

Defined in [Student.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Student.java:1).

- Has `id`, `name`
- Owns `OneToOne` relationship with `AdmissionRecord`
- Has inverse `ManyToMany` relationship with `Subject`

Meaning:

- Every student gets an admission record when created.
- A student can be enrolled in multiple subjects.

### AdmissionRecord

Defined in [AdmissionRecord.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/AdmissionRecord.java:1).

- Has `id`, `fees`
- Inverse side of the `OneToOne` relationship with `Student`

Meaning:

- AdmissionRecord stores student financial onboarding data.
- Persistence is driven from the student side.

## 5. Relationship rules implemented in the project

### Mandatory link: Professor -> Subject

- A professor must be created with at least one subject.
- This is enforced in [ProfessorRequestDTO.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/dto/professor/ProfessorRequestDTO.java:1) using `@NotEmpty` and `@Valid`.
- Subject creation is currently tied to professor creation or subject assignment flows.

### Mandatory link: Student -> AdmissionRecord

- Student creation automatically creates and links an admission record.
- This logic lives in [StudentService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/StudentService.java:1) and [AdmissionService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/AdmissionService.java:1).
- The default fee is `1000`.

### Optional link: Subject <-> Student

- Subjects may exist with zero students.
- Students can later be enrolled into subjects through a patch endpoint.

## 6. What the API currently supports

### Professor operations

Defined in [ProfessorController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/ProfessorController.java:1).

- `POST /professors`
  - Create a professor with at least one subject
- `POST /professors/{professorId}/subjects`
  - Add new subjects to an existing professor
  - Existing subject titles are ignored by service logic
- `GET /professors`
  - List all professors
- `GET /professors/{professorId}`
  - Get one professor
- `DELETE /professors/{professorId}`
  - Delete a professor and cascade-delete their subjects
- `DELETE /professors/{professorId}/subjects/{subjectId}`
  - Remove a subject from a professor
  - Because subjects cannot exist without a professor, this becomes actual subject deletion

### Student operations

Defined in [StudentController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/StudentController.java:1).

- `GET /students`
  - List all students
- `GET /students/{studentId}`
  - Get one student
- `POST /students`
  - Create a student
  - Automatically creates an `AdmissionRecord`
- `DELETE /students/{studentId}`
  - Delete a student
  - Admission record is also removed because of cascade and orphan removal

### Subject operations

Defined in [SubjectController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/SubjectController.java:1).

- `GET /subjects`
  - List all subjects
- `GET /subjects/{subjectId}`
  - Get one subject
- `PATCH /subjects/{subjectId}/professors/{professorId}`
  - Reassign a subject to a different professor
- `PATCH /subjects/{subjectId}/students/{studentId}`
  - Enroll a student into a subject

## 7. What is possible in business terms

Using the current codebase, the project can:

- Register a professor together with their initial subjects
- Enforce that professors are never created without subjects
- Register students with automatic admission records
- Assign students to subjects after student creation
- Reassign a subject from one professor to another
- Fetch detailed professor, student, and subject profiles
- Prevent recursive API payloads using summary DTOs
- Delete a professor and automatically remove their subjects
- Delete a student and automatically remove their admission record
- Keep student-subject enrollment data in a join table
- Filter out already-existing subject titles when adding new subjects to a professor

## 8. DTO design and API shape

DTOs are in `src/main/java/.../dto`.

The project uses three DTO categories:

- `RequestDTO`
  - Validate incoming API input
- `ResponseDTO`
  - Return rich object graphs for a resource
- `SummaryDTO`
  - Return leaf nodes to stop recursion

Examples:

- `ProfessorResponseDTO` contains `SubjectSummaryDTO`
- `SubjectResponseDTO` contains `ProfessorSummaryDTO` and `StudentSummaryDTO`
- `StudentResponseDTO` contains `SubjectSummaryDTO` and `AdmissionRecordSummaryDTO`
- `AdmissionRecordResponseDTO` contains `StudentSummaryDTO`

This is the main recursion-avoidance strategy in the project.

## 9. Key architectural decisions learned from code comments

This section consolidates the design notes written in code comments and tests.

### 1. Summary DTOs are the recursion guard

Learned from:

- [ProfessorController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/ProfessorController.java:15)
- [SubjectController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/SubjectController.java:11)
- Multiple DTO classes

Takeaway:

- Avoid returning entities directly.
- Use leaf/summary DTOs to stop both ModelMapper recursion and JSON circular reference issues.

### 2. Validation should fail at the API boundary

Learned from:

- [ProfessorRequestDTO.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/dto/professor/ProfessorRequestDTO.java:11)
- [ProfessorController.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/controller/ProfessorController.java:34)

Takeaway:

- `@Valid`, `@NotBlank`, and `@NotEmpty` are used as a DTO firewall.
- Invalid payloads should be rejected before service logic executes.
- Nested validation matters, especially for `List<SubjectRequestDTO>`.

### 3. Redundant direct Professor <-> Student links were intentionally removed

Learned from:

- [Professor.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Professor.java:28)
- [Student.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Student.java:27)
- [ProfessorRequestDTO.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/dto/professor/ProfessorRequestDTO.java:51)

Takeaway:

- The system treats `Professor -> Subject -> Student` as the single source of truth.
- Storing a separate professor-student relationship would create redundancy, stale data, normalization issues, and recursion problems.

### 4. Subject is the owning side for professor assignment

Learned from:

- [Subject.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/entity/Subject.java:23)
- [ProfessorService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/ProfessorService.java:19)
- [SubjectService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/SubjectService.java:47)

Takeaway:

- `subject.setProfessor(professor)` is the critical write for persistence.
- The owning side must be updated for the foreign key to change correctly.

### 5. Orphan removal is aggressive and affects reassignment logic

Learned from:

- [SubjectService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/SubjectService.java:56)
- [SubjectServiceTests.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/test/java/com/Module3/Practice/CollegeManagement/service/SubjectServiceTests.java:56)

Takeaway:

- Removing a subject from the old professor collection during reassignment can trigger hard delete because `orphanRemoval = true`.
- The safer pattern is:
  - update the owning side (`subject.setProfessor(newProfessor)`)
  - sync the new professor collection
  - reload from DB when needed to avoid stale cache assumptions

### 6. Persistence context management matters in integration tests

Learned from:

- [StudentServiceIntegrationTests.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/test/java/com/Module3/Practice/CollegeManagement/service/StudentServiceIntegrationTests.java:24)
- [ProfessorServiceIntegrationTests.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/test/java/com/Module3/Practice/CollegeManagement/service/ProfessorServiceIntegrationTests.java:69)

Takeaway:

- JPA L1 cache can show stale in-memory relationships.
- `flush`, `clear`, and sometimes `detach` are used to force assertions against database truth.
- This project has explicitly learned that persistence-context state and database state are not always the same during a transaction.

### 7. Delete flows rely on DB cleanup plus cache refresh strategy

Learned from:

- [StudentServiceIntegrationTests.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/test/java/com/Module3/Practice/CollegeManagement/service/StudentServiceIntegrationTests.java:74)
- [ProfessorServiceIntegrationTests.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/test/java/com/Module3/Practice/CollegeManagement/service/ProfessorServiceIntegrationTests.java:101)

Takeaway:

- Student deletion relies on cascade/orphan behavior and join-table cleanup.
- Subject and professor deletion may need `detach + flush + clear` in tests to verify fresh state correctly.

### 8. `flatMap` is used intentionally when nesting Optional-returning lookups

Learned from:

- [SubjectService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/SubjectService.java:47)

Takeaway:

- `flatMap` avoids ending up with `Optional<Optional<T>>`.
- This is relevant in multi-step lookup flows like professor-then-subject assignment.

### 9. The service layer avoids circular dependencies between services

Learned from:

- [SubjectService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/SubjectService.java:23)

Takeaway:

- `SubjectService` uses `ProfessorRepository` directly instead of depending on `ProfessorService`.
- This reduces coupling and avoids `BeanCurrentlyInCreationException`.

### 10. Projection queries are used for small performance wins

Learned from:

- [SubjectRepository.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/repository/SubjectRepository.java:1)
- [ProfessorService.java](/Users/shubhgaur/Documents/SpringBOOT/CollegeManagement/src/main/java/com/Module3/Practice/CollegeManagement/service/ProfessorService.java:100)

Takeaway:

- Existing subject detection fetches only subject titles instead of full entities.
- This keeps the duplicate-check path lighter.

## 10. What the tests show the project is intended to guarantee

From the test suite, the intended guarantees are:

- New admission records default to `fees = 1000`
- AdmissionRecord must be linked back to Student for mapping consistency
- Creating a professor cascades subject persistence
- Removing a subject should immediately evict it from the persistence context
- Adding duplicate subject titles to a professor should not create duplicates
- Reassigning a subject should move it between professor collections
- Assigning a student to a subject should update the join table and the inverse side
- Deleting a student should also delete the admission record
- Deleting a professor should remove linked subjects and clean student relationships
- Deleting a subject should leave student subject sets clean after refresh

## 11. Known limitations and gaps

### Implemented but not exposed

- Admission record DTOs and repository exist, but there is no `AdmissionController`.
- Financial update flows are described as future work in comments.

### Environment limitations

- The app is tightly coupled to a live MySQL database in the current configuration.
- Tests are not self-contained because they do not switch to an in-memory database for test runs.

### API limitations

- There is no update endpoint for professor or student profile fields.
- There is no dedicated endpoint to remove a student from a subject.
- There is no dedicated endpoint to create a standalone subject directly.
- There is no explicit exception handling layer for validation or database integrity errors.
- `assignNewSubjectsOnly` silently ignores existing titles instead of returning a conflict report.

## 12. Suggested future improvements

- Add a `README` or API guide with sample request/response payloads.
- Add an `AdmissionController` for fee management and admission-specific workflows.
- Add global exception handling with structured error responses.
- Use a test database profile, ideally H2 or a dedicated MySQL test container.
- Add endpoints for:
  - removing a student from a subject
  - updating fees
  - updating professor and student basic details
  - searching/filtering resources
- Add explicit transaction and fetch-strategy review to reduce possible N+1 issues in larger datasets.

## 13. Summary

This project already supports a meaningful college management workflow centered on:

- professor creation and subject ownership
- student creation with admission setup
- subject reassignment
- subject enrollment
- cascade and orphan-removal behavior
- recursion-safe DTO responses

The most important lessons captured in the code comments are about:

- DTO-driven validation
- avoiding redundant relationships
- treating owning sides correctly in JPA
- handling orphan removal carefully
- respecting persistence-context behavior during updates and deletes
