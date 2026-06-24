package com.shubh.module4.Prod_Ready_Features.clients.impl;

import com.shubh.module4.Prod_Ready_Features.clients.EmployeeClient;
import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EmployeeClientImpl implements EmployeeClient {

    RestClient restClient;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        try {
            return restClient.get()
                    /* * URL CONCATENATION GOTCHA:
                     * Ensure your base_url configuration in the RestClient bean ends with a trailing slash '/'.
                     * If base_url is "https://api.example.com/v1" and uri is "employees", Spring resolves it
                     * incorrectly to "https://api.example.com/v1employees", throwing a 404.
                     */
                    .uri("employees")
                    .retrieve()
                    /* * WHY WE NEED ParameterizedTypeReference:
                     *
                     * 1. THE PROBLEM (TYPE ERASURE):
                     * Java uses "Type Erasure" at compile time. This means generic type information like
                     * `<EmployeeDTO>` is stripped away before execution. At runtime, the JVM only sees a raw `List`.
                     * If we passed `List.class` here, Spring's JSON parser (Jackson) wouldn't know what data type
                     * belongs inside the list, defaulting to converting the JSON objects into a `List<LinkedHashMap>`.
                     * This causes a `ClassCastException` downstream when your code expects an `EmployeeDTO`.
                     *
                     * 2. THE SOLUTION (ANONYMOUS INNER CLASS LOOPHOLE):
                     * By instantiating `new ParameterizedTypeReference<List<EmployeeDTO>>() {}`, we are creating
                     * an "anonymous inner class" (notice the empty trailing curly braces `{}`).
                     * Under Java's rules, while standard generic instances lose their type data, the superclass
                     * metadata of an anonymous inner class preserves the full generic signature inside the compiled
                     * bytecode.
                     *
                     * 3. WHAT IT DOES:
                     * Spring captures this preserved metadata at runtime, safely bypassing type erasure. It explicitly
                     * informs Jackson: "This JSON array must be mapped into concrete EmployeeDTO objects inside a List."
                     */
                    .body(new ParameterizedTypeReference<List<EmployeeDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch employees from client", e);
        }
    }
}