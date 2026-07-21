package com.Shubh.Module7.M7_TestingMethodologies.controller;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import com.Shubh.Module7.M7_TestingMethodologies.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;



class EmployeeControllerTestIntegrationTests extends BaseIntegrationTests {
    @Autowired
    EmployeeRepository employeeRepository;

    Employee testEmployee;
    EmployeeDTO testEmployeeDTO;

    @Autowired
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        testEmployee = new Employee(
                1L,
                "Shubh",
                "shubh@xyz.com",
                10000.0
        );

        testEmployeeDTO = new EmployeeDTO(
                1L,
                "Shubh",
                "shubh@xyz.com",
                10000.0
        );
    }

    @Test
    void testGetAllEmployees() {

        // ---------------------- Arrange ----------------------

        // Persist test data into the Testcontainers database.
        employeeRepository.save(testEmployee);

        // -------------------- Act & Assert --------------------

        webTestClient.get()

                // Call only the endpoint. WebTestClient already knows the
                // application's host and random port.
                .uri("/employees")

                // Send the HTTP request.
                .exchange()

                // Verify that the request completed successfully.
                .expectStatus().isOk()

                // Convert the JSON response body into List<EmployeeDTO>.
                .expectBodyList(EmployeeDTO.class)

                // Consumer<List<EmployeeDTO>> used to perform assertions on the
                // deserialized response body.
                .value(employeeDTOs -> {

                    assertThat(employeeDTOs).isNotNull().hasSize(1);

                    EmployeeDTO fetchedEmployee = employeeDTOs.getFirst();

                    // If equals() and hashCode() are implemented correctly,
                    // these individual field assertions can be replaced with:
                    //
                    // assertThat(fetchedEmployee).isEqualTo(testEmployeeDTO);
                    assertThat(fetchedEmployee.getId()).isEqualTo(testEmployeeDTO.getId());
                    assertThat(fetchedEmployee.getName()).isEqualTo(testEmployeeDTO.getName());
                    assertThat(fetchedEmployee.getEmail()).isEqualTo(testEmployeeDTO.getEmail());
                    assertThat(fetchedEmployee.getSalary()).isEqualTo(testEmployeeDTO.getSalary());
                });
    }

    @Test
    void testGetEmployeeById_Success() {

        // ---------------------- Arrange ----------------------

        employeeRepository.save(testEmployee);

        Long id = testEmployeeDTO.getId();

        // -------------------- Act & Assert --------------------

        webTestClient.get()

                // URI templates require named placeholders (e.g. {id}),
                // unlike SLF4J loggers which use anonymous {} placeholders.
                .uri("/employees/{id}", id)

                .exchange()

                // Verify that the request completed successfully.
                .expectStatus().isOk()

                // Deserialize the JSON response into an EmployeeDTO and verify
                // it matches the expected object.
                .expectBody(EmployeeDTO.class)
                .value(fetchedEmployeeDTO ->
                        assertThat(fetchedEmployeeDTO).isEqualTo(testEmployeeDTO));
    }

    @Test
    void testGetEmployeeById_Failure() {
        // No employee is inserted into the database, so requesting this ID
        // should result in a 404 Not Found response.
        Long id = testEmployeeDTO.getId();

        webTestClient.get()
                .uri("/employees/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateNewEmployee_success() {

        // ---------------------- Arrange ----------------------
        // No database setup is required. The employee is created by sending an
        // HTTP POST request to the controller.

        // -------------------- Act & Assert --------------------

        webTestClient.post()

                // Target the employee creation endpoint.
                .uri("/employees")

                // Serialize(Memory Object to JSON) the EmployeeDTO into JSON and send it as the request
                // body. Spring automatically deserializes it back into an
                // EmployeeDTO parameter annotated with @RequestBody.
                .bodyValue(testEmployeeDTO)

                // Send the HTTP request.
                .exchange()

                // Verify that a new employee was created successfully.
                .expectStatus().isCreated()

                // Verify selected fields in the JSON response using JSONPath.
                // JSONPath provides a convenient way to assert individual JSON
                // properties without deserializing the entire response into an
                // EmployeeDTO object.
                .expectBody()
                .jsonPath("$.id").isEqualTo(testEmployeeDTO.getId())
                .jsonPath("$.email").isEqualTo(testEmployeeDTO.getEmail());
    }

    @Test
    void testCreateNewEmployee_failure() {
        employeeRepository.save(testEmployee);

        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void testUpdateEmployeeByID_success() {
        employeeRepository.save(testEmployee);
        Long id = testEmployeeDTO.getId();

        webTestClient.put()
                .uri("/employees/{id}", id)
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeDTO.class)
                .value(fetchedEmployeeDTO ->
                        assertThat(fetchedEmployeeDTO).isEqualTo(testEmployeeDTO));
    }

    @Test
    void testUpdateEmployeeByID_failureEmployeeNotfound() {
        Long id = testEmployeeDTO.getId();

        webTestClient.put()
                .uri("/employees/{id}", id)
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateEmployeeByID_failureEmailModification_throwsException() {
        // save the employee
        employeeRepository.save(testEmployee);

        Long id = testEmployeeDTO.getId();

        // tamper the email
        testEmployeeDTO.setEmail("abc@xyz.com");

        webTestClient.put()
                .uri("/employees/{id}", id)
                .bodyValue(testEmployeeDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    void testDeleteEmployeeByID_success() {
        // save the employee
        employeeRepository.save(testEmployee);

        Long id = testEmployeeDTO.getId();

        webTestClient.delete()
                .uri("/employees/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }


    @Test
    void testDeleteEmployeeByID_failureNotFound() {
        Long id = testEmployeeDTO.getId();

        webTestClient.delete()
                .uri("/employees/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }


    // JUnit executes test methods sequentially by default, so this cleanup runs
    // only after the current test has finished. Removing all records ensures the
    // next test starts with a clean database and remains independent of the
    // execution order.
    @AfterEach()
    void tearDown() {
        employeeRepository.deleteAll();
    }
}