package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import com.Module3.Practice.CollegeManagement.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class AdmissionServiceTests {

    @Autowired
    private AdmissionService admissionService;

    @Test
    @DisplayName("Verify Business Rules: Admission record should be built with correct defaults and linked to student")
    void testInitialiseAdmissionRecord() {
        // ARRANGE
        // We simulate a student that exists in the system
        Student mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setName("Test Student");

        // ACT
        // The service logic should handle the creation and bidirectional linking
        AdmissionRecord record = admissionService.initialiseAdmissionRecord(mockStudent);

        // ASSERT
        // 1. Verify Default Fees (Business Rule)
        assertThat(record.getFees())
                .as("New admission records should default to 1000 fees")
                .isEqualTo(1000);

        // 2. Verify Entity Relationship
        // This is the "Source of Truth" that our Generic Mapper relies on
        assertThat(record.getStudent())
                .as("The admission record must be linked to the Student entity")
                .isNotNull();

        assertThat(record.getStudent().getId())
                .as("The linked student must have a valid ID for the DTO flattening to work")
                .isEqualTo(1L);

        assertThat(record.getStudent().getName())
                .isEqualTo("Test Student");
    }

    /*
     * NOTE ON FUTURE DTO TESTS:
     * When testing the Controller or a service method that returns AdmissionRecordDTO,
     * remember that the assertion will change from:
     * * assertThat(record.getStudent().getId())
     * * to:
     * * assertThat(recordDto.getStudentId())
     * * because our Generic ModelMapper flattens the relationship to break circular dependencies.
     */
}