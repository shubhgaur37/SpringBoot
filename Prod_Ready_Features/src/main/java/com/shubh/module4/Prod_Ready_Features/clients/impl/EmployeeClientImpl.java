package com.shubh.module4.Prod_Ready_Features.clients.impl;

import com.shubh.module4.Prod_Ready_Features.advice.ApiResponse;
import com.shubh.module4.Prod_Ready_Features.clients.EmployeeClient;
import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
                    .body(new ParameterizedTypeReference<ApiResponse<List<EmployeeDTO>>>() {});

                    return employeeDTOList.getData();

        } catch (Exception e) {
            /* * 💡 DECOUPLED & ENCAPSULATED ERROR HANDLING:
             * We intentionally catch a broad Exception here and avoid extracting or parsing the specific
             * upstream error schema. This completely encapsulates the client layer; our application domain
             * does not need to know the exact internal reasons behind a remote failure.
             * This insulates our system from breaking if the external service changes its error contracts.
             */
            throw new RuntimeException("Failed to fetch employees from client", e);
        }
    }
}