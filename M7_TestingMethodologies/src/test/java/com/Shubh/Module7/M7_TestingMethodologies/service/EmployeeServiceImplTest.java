package com.Shubh.Module7.M7_TestingMethodologies.service;

import com.Shubh.Module7.M7_TestingMethodologies.dto.EmployeeDTO;
import com.Shubh.Module7.M7_TestingMethodologies.entity.Employee;
import com.Shubh.Module7.M7_TestingMethodologies.exception.DuplicateResourceException;
import com.Shubh.Module7.M7_TestingMethodologies.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void testGetAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(mockEmployee));

        employeeService.getAllEmployees();

        verify(employeeRepository, times(1)).findAll();
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

    @Test
    void testCreateNewEmployee_WithExistingEmail_ThrowsException() {
        // Arrange
        when(employeeRepository.findByEmail(mockEmployeeDTO.getEmail())).thenReturn(List.of(mockEmployee));

        // Act and assert

        assertThatThrownBy(() -> employeeService.createNewEmployee(mockEmployeeDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Employee already exists with email: " + mockEmployeeDTO.getEmail());

        // check save should never be called with any argument
        verify(employeeRepository, never()).save(any());
    }


    @Test
    void testUpdateEmployee_NotExists_ThrowsException() {

        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Act and assert
        Long id = mockEmployeeDTO.getId();
        assertThatThrownBy(() -> employeeService.updateEmployeeByID(id, mockEmployeeDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: " + mockEmployee.getId());

        // check save should never be called with any argument
        verify(employeeRepository, never()).save(any());
    }


    @Test
    void testUpdateEmployeeEmail_ThrowsException() {
        Long id = mockEmployeeDTO.getId();
        mockEmployeeDTO.setEmail("hola@amigo.com");
        // Arrange
        when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee));

        // Act and assert
        assertThatThrownBy(() -> employeeService.updateEmployeeByID(id, mockEmployeeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email of the employee cannot be updated");

        // check save should never be called with any argument
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployeeDetails_updatesEmployee() {

        Long id = mockEmployee.getId();

        // Updated details (email must remain unchanged)
        EmployeeDTO updatedEmployeeDTO = new EmployeeDTO(
                id,
                "Rahul",
                "shubh@xyz.com",
                25000.0
        );

        Employee updatedEmployee = new Employee(
                id,
                "Rahul",
                "shubh@xyz.com",
                25000.0
        );

        // ---------------------- Arrange (Stubbing) ----------------------

        when(employeeRepository.findById(id))
                .thenReturn(Optional.of(mockEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(updatedEmployee);

        // --------------------------- Act ---------------------------

        EmployeeDTO result = employeeService.updateEmployeeByID(id, updatedEmployeeDTO);

        // -------------------------- Assert --------------------------

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Rahul");
        assertThat(result.getEmail()).isEqualTo("shubh@xyz.com");
        assertThat(result.getSalary()).isEqualTo(25000.0);

        // -------------------- ArgumentCaptor --------------------

        ArgumentCaptor<Employee> employeeCaptor =
                ArgumentCaptor.forClass(Employee.class);

        verify(employeeRepository).save(employeeCaptor.capture());

        Employee savedEmployee = employeeCaptor.getValue();

        // Verify that the service created and passed the expected Employee to the
        // repository. Since Employee uses Lombok's @EqualsAndHashCode, a single
        // object comparison verifies all fields included in the generated equals()
        // method.
        //
        // Caveat:
        // This assertion is only as good as the equals() implementation. If
        // @EqualsAndHashCode is configured to exclude fields (or include only the
        // ID), differences in those excluded fields will not be detected by this
        // assertion. In such cases, assert the individual fields explicitly.
        assertThat(savedEmployee).isEqualTo(updatedEmployee);
        assertThat(savedEmployee.getId()).isEqualTo(id);
        assertThat(savedEmployee.getName()).isEqualTo("Rahul");
        assertThat(savedEmployee.getEmail()).isEqualTo("shubh@xyz.com");
        assertThat(savedEmployee.getSalary()).isEqualTo(25000.0);


        verify(employeeRepository).findById(id);
        verify(employeeRepository).save(any(Employee.class));
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    void deleteEmployee_WhenEmployeeNotPresent_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        Long id = mockEmployee.getId();
        assertThatThrownBy(() -> employeeService.deleteEmployeeByID(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: " + id);

        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void deleteEmployee_WhenEmployeeExists() {

        Long id = mockEmployee.getId();

        // ---------------------- Arrange (Stubbing) ----------------------

        // Stub findById() only for this specific ID. Mockito matches arguments
        // exactly unless argument matchers (e.g. anyLong()) are used.
        when(employeeRepository.findById(id))
                .thenReturn(Optional.of(mockEmployee));

        // --------------------------- Act ---------------------------

        // Invoking deleteEmployeeByID() with any ID other than 'id' would cause
        // the repository to be called with an unstubbed argument. Since Mockito
        // uses strict stubbing by default, it throws a PotentialStubbingProblem
        // instead of silently returning Optional.empty(). This helps detect
        // incorrect test setup early.
        boolean isDeleted = employeeService.deleteEmployeeByID(id);

        // -------------------------- Assert --------------------------

        assertThat(isDeleted).isTrue();

        // -------------------------- Verify --------------------------

        verify(employeeRepository).findById(id);
        verify(employeeRepository).deleteById(id);
        verifyNoMoreInteractions(employeeRepository);
    }
}