package com.Module3.Practice.CollegeManagement.repository;

import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRecordRepository extends JpaRepository<AdmissionRecord, Long> {
}