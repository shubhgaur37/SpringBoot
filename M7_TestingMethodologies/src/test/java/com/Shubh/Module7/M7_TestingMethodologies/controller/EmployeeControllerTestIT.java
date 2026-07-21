package com.Shubh.Module7.M7_TestingMethodologies.controller;

import com.Shubh.Module7.M7_TestingMethodologies.TestContainersConfiguration;
import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import com.Shubh.Module7.M7_TestingMethodologies.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;


// Automatically configures and exposes a WebTestClient bean for integration
// tests. Required in Spring Boot 4.x when using WebTestClient.
@AutoConfigureWebTestClient

// Starts the full application on a random HTTP port so WebTestClient can send
// real HTTP requests. The default MOCK environment does not start an embedded
// web server, so WebTestClient cannot connect to one.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
class EmployeeControllerTestIT {

    // Injected by @AutoConfigureWebTestClient. Requires a running embedded web
    // server; WebEnvironment.MOCK does not create this bean, causing startup to
    // fail with NoSuchBeanDefinitionException.
    @Autowired
    WebTestClient webTestClient;

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

    // JUnit executes test methods sequentially by default, so this cleanup runs
    // only after the current test has finished. Removing all records ensures the
    // next test starts with a clean database and remains independent of the
    // execution order.
    @AfterEach()
    void tearDown(){
        employeeRepository.deleteAll();
    }
}