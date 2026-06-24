package com.shubh.module4.Prod_Ready_Features.clients.impl;

import com.shubh.module4.Prod_Ready_Features.advice.ApiResponse;
import com.shubh.module4.Prod_Ready_Features.clients.EmployeeClient;
import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;
import com.shubh.module4.Prod_Ready_Features.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class EmployeeClientImpl implements EmployeeClient {

    RestClient restClient;

    // To resolve ambiguity when multiple rest clients are available in the context
    public EmployeeClientImpl(@Qualifier("employeeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
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

            return employeeDTOList.getData();

        } catch (Exception e) {
            /* * 💡 DECOUPLED & ENCAPSULATED ERROR HANDLING:
             * We intentionally catch a broad Exception here and avoid extracting or parsing the specific
             * upstream error schema. This completely encapsulates the client layer; our application domain
             * does not need to know the exact internal reasons behind a remote failure.
             * This insulates our system from breaking if the external service changes its error contracts.
             */
            throw new RuntimeException("Failed to fetch employee from server[rest-client]", e);
        }
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        try{
            ApiResponse<EmployeeDTO> employeeResponse = restClient.get()
                    .uri("employees/{employeeId}",id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<EmployeeDTO>>() {
                    });
            return employeeResponse.getData();
        }
        catch (Exception e){
            throw new RuntimeException("Failed to fetch employee from server[rest-client]", e);
        }
    }

    @Override
    public EmployeeDTO createNewEmployee(EmployeeDTO inputDTO) {
        try{
            ApiResponse<EmployeeDTO> employeeResponse = restClient.post()
                    .uri("employees")
                    .body(inputDTO) // request body
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (req,res) -> {
                            /* 💡 THE STREAM RULE: The body is a transient network stream, not an in-memory container.
                               Calling readAllBytes() decodes, drains, and permanently closes this connection stream.

                               🟢 WHY IT WORKS PERFECTLY HERE:
                               We throw 'ResourceNotFoundException' on the very next line. This completely breaks out of
                               the execution chain, meaning Spring never attempts to read from this closed stream again.

                               🔴 WHEN THIS WILL FAIL:
                               If you remove the 'throw' line (e.g., trying to log the error and let the execution continue),
                               the code will crash down at the .body(...) mapper. Spring will try to read the response to
                               parse it into your DTO, find the stream empty and closed, and throw a "Stream Closed" IOException. */
                                System.out.println(new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                                throw new ResourceNotFoundException("could not create employee due to client error");
                            })
                    .body(new ParameterizedTypeReference<ApiResponse<EmployeeDTO>>() {
                    }); // response body
            return employeeResponse.getData();
        }
        catch (Exception e){
        /* ⚠️ Because ResourceNotFoundException isn't caught explicitly above,
           this generic block will intercept it and wrap it here. */
            throw new RuntimeException("Failed to fetch employee from server[rest-client]:" + e.getMessage());
        }
    }
}