package com.Shubh.Module7.M7_TestingMethodologies.service;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import com.Shubh.Module7.M7_TestingMethodologies.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


// Enables Mockito support for JUnit 5.
//
// Before each test, Mockito automatically:
//   • Creates mock objects for fields annotated with @Mock.
//   • Creates spy objects for fields annotated with @Spy.
//   • Creates the class under test for @InjectMocks and injects the mocks/spies
//     into its constructor, fields, or setter methods.
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    // Creates a mock implementation of EmployeeRepository.
    // All method calls return Mockito defaults unless explicitly stubbed.
    @Mock
    EmployeeRepository employeeRepository;

    // Creates a spy of ModelMapper.
    //
    // Unlike a mock, a spy executes the real implementation unless explicitly
    // stubbed. We use a spy because ModelMapper is a trusted third-party
    // library whose behavior has already been tested.
    @Spy
    ModelMapper modelMapper;

    // This would fail because EmployeeService is an interface and Mockito
    // cannot instantiate interfaces.
    //
    // @InjectMocks
    // EmployeeService employeeService;

    // Creates the class under test and injects all available mocks and spies
    // into it.
    @InjectMocks
    EmployeeServiceImpl employeeService;

    // Test fixture shared across test methods.
    //
    // Mockito creates a new instance of this test class for every test method,
    // so these fields are recreated and do not retain state between tests.
    Employee mockEmployee;

    // Never initialize objects here using injected dependencies (such as
    // ModelMapper), because Mockito has not yet initialized those fields.
    //
    // EmployeeDTO mockEmployeeDTO =
    //         modelMapper.map(mockEmployee, EmployeeDTO.class);
    EmployeeDTO mockEmployeeDTO;

    @BeforeEach
    void setup() {

        // Create reusable test data.
        mockEmployee = new Employee(
                1L,
                "Shubh",
                "shubh@xyz.com",
                10000.0
        );

        // Convert the entity into a DTO using the real ModelMapper.
        mockEmployeeDTO = modelMapper.map(mockEmployee, EmployeeDTO.class);
    }


    @Test
    void test_getEmployeeById_returnsEmployeeDTO_withValidEmployeeID() {

        Long id = 1L;

        // ---------------------- Arrange (Stubbing) ----------------------

        // Simulate finding an employee with the given ID.
        when(employeeRepository.findById(id))
                .thenReturn(Optional.of(mockEmployee));


        // --------------------------- Act ---------------------------

        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);


        // -------------------------- Assert --------------------------

        assertThat(employeeDTO).isNotNull();
        assertThat(employeeDTO.getId()).isEqualTo(1);
        assertThat(employeeDTO.getName()).isEqualTo("Shubh");
        assertThat(employeeDTO.getEmail()).isEqualTo("shubh@xyz.com");
        assertThat(employeeDTO.getSalary()).isEqualTo(10000);


        // -------------------------- Verify --------------------------

        // Verify that findById() was invoked exactly once with the expected ID.
        verify(employeeRepository, times(1)).findById(id);

        // Verify that save() was never invoked because this operation only
        // retrieves an employee.
        verify(employeeRepository, never()).save(any(Employee.class));

        // Verify that no additional interactions occurred with the repository.
        verifyNoMoreInteractions(employeeRepository);
    }


    @Test
    void testCreateNewEmployee_WhenValidEmployee_ThenReturnEmployeeDTO() {

        // ---------------------- Arrange (Stubbing) ----------------------

        // When the repository checks whether an employee already exists with the
        // supplied email, return an empty list to simulate "email not found".
        when(employeeRepository.findByEmail(anyString())).thenReturn(List.of());

        // Match any Employee passed to save(). The service creates a new Employee
        // entity using ModelMapper, so we cannot reference that exact object while
        // stubbing. Regardless of which Employee is passed, Mockito returns
        // mockEmployee.
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);


        // --------------------------- Act ---------------------------

        EmployeeDTO employeeDTO = employeeService.createNewEmployee(mockEmployeeDTO);


        // -------------------------- Assert --------------------------

        assertThat(employeeDTO).isNotNull();
        assertThat(employeeDTO.getId()).isEqualTo(1);
        assertThat(employeeDTO.getName()).isEqualTo("Shubh");
        assertThat(employeeDTO.getEmail()).isEqualTo("shubh@xyz.com");
        assertThat(employeeDTO.getSalary()).isEqualTo(10000);


        // Verify that save() was invoked exactly once with an Employee object.
        verify(employeeRepository).save(any(Employee.class));


        // -------------------- ArgumentCaptor --------------------
        //
        // verify(...).save(any(Employee.class)) only tells us that save() was
        // called. It does NOT let us inspect the Employee object that the service
        // actually passed to the repository.
        //
        // ArgumentCaptor captures that argument so we can verify its state.
        //
        // Typical use cases:
        //   • Verify fields copied from a DTO to an Entity.
        //   • Verify that the service generated or modified values before saving
        //     (e.g. timestamps, encoded passwords, status flags).
        //   • Verify that the correct object was passed to another dependency.

        // Capture the Employee argument passed to repository.save(...).
        ArgumentCaptor<Employee> employeeArgumentCaptor =
                ArgumentCaptor.forClass(Employee.class);

        verify(employeeRepository).save(employeeArgumentCaptor.capture());

        // Returns the captured Employee.
        // If save() was called multiple times, use getAllValues() instead.
        Employee savedEmployee = employeeArgumentCaptor.getValue();

        assertThat(savedEmployee).isNotNull();
        assertThat(savedEmployee.getId()).isEqualTo(1);
        assertThat(savedEmployee.getName()).isEqualTo("Shubh");
        assertThat(savedEmployee.getEmail()).isEqualTo("shubh@xyz.com");
        assertThat(savedEmployee.getSalary()).isEqualTo(10000);
    }
}