package com.shubh.module4.Prod_Ready_Features.clients.impl;

import com.shubh.module4.Prod_Ready_Features.advice.ApiResponse;
import com.shubh.module4.Prod_Ready_Features.clients.EmployeeClient;
import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;
import com.shubh.module4.Prod_Ready_Features.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class EmployeeClientImpl implements EmployeeClient {

    RestClient restClient;

    Logger log = LoggerFactory.getLogger(EmployeeClientImpl.class);

    // To resolve ambiguity when multiple rest clients are available in the context
    public EmployeeClientImpl(@Qualifier("employeeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.trace("Trying to retrieve all employees in getAllEmployees");
        try {
            ApiResponse<List<EmployeeDTO>> employeeDTOList = restClient.get()
                    /* * URL CONCATENATION GOTCHA:
                     * Ensure your base_url configuration in the RestClient bean ends with a trailing slash '/'.
                     * If base_url is "https://api.example.com/v1" and uri is "employees", Spring resolves it
                     * incorrectly to "https://api.example.com/v1employees", throwing a 404.
                     */
                    .uri("employees")
                    .retrieve()
                    /* * WHY WE NEED ParameterizedTypeReference (UPDATED FOR WRAPPERS):
                     *
                     * 1. THE PROBLEM (TYPE ERASURE):
                     * Java uses "Type Erasure" at compile time. Generic information like <ApiResponse<List<EmployeeDTO>>>
                     * is stripped away. At runtime, the JVM only sees a raw ApiResponse containing a raw List.
                     * Without instructions, Jackson would default to converting array elements into standard
                     * LinkedHashMap instances instead of EmployeeDTO objects, causing a ClassCastException downstream.
                     *
                     * 2. THE SOLUTION (ANONYMOUS INNER CLASS LOOPHOLE):
                     * By instantiating an anonymous inner class via the trailing {}, the superclass metadata permanently
                     * locks the full, multi-layered generic signature directly inside the compiled class bytecode.
                     *
                     * 3. EXPLICIT TYPING VS DIAMOND OPERATOR:
                     * Because we are now dealing with a complex nested wrapper (ApiResponse -> List -> EmployeeDTO),
                     * the diamond operator (<>) fails to infer type arguments properly. Explicitly specifying the full type
                     * guarantees that Jackson accurately navigates and unwraps every nested generic layer.
                     */
                    .body(new ParameterizedTypeReference<ApiResponse<List<EmployeeDTO>>>() {
                    });
            log.debug("Successfully retrieved the employees in getAllEmployees");
            // passing data for placeholders in trace
            log.trace("Retrieved Employees list from getAllEmployees : {}, {}, {}", employeeDTOList.getData(), "Hello", 5);
            return employeeDTOList.getData();

        } catch (Exception e) {
            /* * 💡 DECOUPLED & ENCAPSULATED ERROR HANDLING:
             * We intentionally catch a broad Exception here and avoid extracting or parsing the specific
             * upstream error schema. This completely encapsulates the client layer; our application domain
             * does not need to know the exact internal reasons behind a remote failure.
             * This insulates our system from breaking if the external service changes its error contracts.
             */
            log.error("Exception occurred in getAllEmployess", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        log.trace("Trying to get employee by id in getEmployeeById with id : {}", id);
        try {
            ApiResponse<EmployeeDTO> employeeResponse = restClient.get()
                    .uri("employees/{employeeId}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.error(new String(res.getBody().readAllBytes()));
                        throw new ResourceNotFoundException("could not find the employee");
                    })
                    .body(new ParameterizedTypeReference<ApiResponse<EmployeeDTO>>() {
                    });
            log.trace("Retrieved Employees : {}", employeeResponse.getData());
            return employeeResponse.getData();
        } catch (Exception e) {
            // log stack trace by passing exception object
            log.error("Exception occurred in getEmployeeById", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public EmployeeDTO createNewEmployee(EmployeeDTO inputDTO) {
        log.trace("Trying to create employee with information: {}", inputDTO);
        try {
            ResponseEntity<ApiResponse<EmployeeDTO>> employeeResponseEntity = restClient.post()
                    .uri("employees")
                    .body(inputDTO) // request body
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                log.debug("4xxClient Error occured in Create New Employee");
                            /* 💡 THE STREAM RULE: The body is a transient network stream, not an in-memory container.
                               Calling readAllBytes() decodes, drains, and permanently closes this connection stream.

                               🟢 WHY IT WORKS PERFECTLY HERE:
                               We throw 'ResourceNotFoundException' on the very next line. This completely breaks out of
                               the execution chain, meaning Spring never attempts to read from this closed stream again.

                               🔴 WHEN THIS WILL FAIL:
                               If you remove the 'throw' line (e.g., trying to log the error and let the execution continue),
                               the code will crash down at the .body(...) mapper. Spring will try to read the response to
                               parse it into your DTO, find the stream empty and closed, and throw a "Stream Closed" IOException. */
                                log.error(new String(res.getBody().readAllBytes()));
                                throw new ResourceNotFoundException("could not create employee due to client error");
                            })
                    .toEntity(new ParameterizedTypeReference<ApiResponse<EmployeeDTO>>() {
                    }); // response entity, gives full access to status codes and headers if required
            log.trace("Sucessfully Created a new employee : {}", employeeResponseEntity.getBody().getData());
            return employeeResponseEntity.getBody().getData();
        } catch (Exception e) {
            log.error("Exception occured in createNewEmployee", e);
        /* ⚠️ Because ResourceNotFoundException isn't caught explicitly above,
           this generic block will intercept it and wrap it here. */
            throw new RuntimeException(e);
        }
    }
}