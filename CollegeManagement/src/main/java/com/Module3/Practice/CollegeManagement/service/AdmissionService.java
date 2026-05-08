package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.repository.AdmissionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdmissionService {
    private final AdmissionRecordRepository admissionRecordRepository;
    private final ModelMapper modelMapper;

    /**
     * Business Logic Isolation:
     * This method handles the "paperwork" logic.
     * We initialize the record here but leave the actual database
     * persistence to StudentService to maintain a single transaction.
     */
    public AdmissionRecord initialiseAdmissionRecord(Student student) {
        AdmissionRecord admissionRecord = new AdmissionRecord();
        admissionRecord.setFees(1000);
        admissionRecord.setStudent(student);
        return admissionRecord;
    }
}