package com.shubh.module4.Prod_Ready_Features;

import com.shubh.module4.Prod_Ready_Features.clients.EmployeeClient;
import com.shubh.module4.Prod_Ready_Features.dto.EmployeeDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
// 🟢 CRITICAL: Enables method ordering based on @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProdReadyFeaturesApplicationTests {

	@Autowired
	private EmployeeClient employeeClient;


	@Test
	@Order(3) // specifying order of testcases
	void getAllEmployeesTest(){
		List<EmployeeDTO> employeeDTOList = employeeClient.getAllEmployees();
		System.out.println(employeeDTOList);
	}

	@Test
	@Order(2)
	void getEmployeeByIdTest(){
		EmployeeDTO employeeDTO = employeeClient.getEmployeeById(1L);
		System.out.println(employeeDTO);
	}

	@Test
	@Order(1)
	void createEmployeeTest(){
		EmployeeDTO inputEmployee = new EmployeeDTO();
		inputEmployee.setName("Krishna");
		inputEmployee.setEmail("kg@gmail.com");
		// inputEmployee.setAge(2); // throws 4XX Bad Request, to test error handling
		inputEmployee.setAge(29);
		inputEmployee.setRole("ADMIN");
		inputEmployee.setSalary(10000D);
		inputEmployee.setDateOfJoining(LocalDate.of(2005,12,1));
		inputEmployee.setIsActive(true);

		EmployeeDTO responseEmployeeDTO = employeeClient.createNewEmployee(inputEmployee);
		System.out.println(responseEmployeeDTO);
	}

}
