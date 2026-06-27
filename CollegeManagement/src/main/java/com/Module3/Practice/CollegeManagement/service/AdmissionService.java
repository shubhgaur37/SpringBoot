package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.repository.AdmissionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdmissionService {
    private final AdmissionRecordRepository admissionRecordRepository;
    private final ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(AdmissionService.class);

    /**
     * Business Logic Isolation:
     * This method handles the "paperwork" logic.
     * We initialize the record here but leave the actual database
     * persistence to StudentService to maintain a single transaction.
     */
    public AdmissionRecord initialiseAdmissionRecord(Student student) {
        // 1. SAFE: Extracts a flat String, bypassing the entity's toString()
        logger.debug("Attempting to initialise admission record for student name: {}", student.getName());

        AdmissionRecord admissionRecord = new AdmissionRecord();
        admissionRecord.setFees(1000);
        admissionRecord.setStudent(student);

        // 2. SAFE: Logs only primitives, completely avoiding a StackOverflowError loop
        logger.trace("Finalised admission record state. Fees: {}, Student Name: {}",
                admissionRecord.getFees(), student.getName());

        return admissionRecord;
    }

}