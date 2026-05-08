package com.Module3.Practice.CollegeManagement.repository;

import com.Module3.Practice.CollegeManagement.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // Fetch only the titles as Strings, not the whole Subject object
    @Query("SELECT s.title FROM Subject s WHERE s.title IN :titles")
    List<String> findExistingTitles(@Param("titles") List<String> titles);
}