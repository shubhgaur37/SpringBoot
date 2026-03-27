package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.BloodGroupStats;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.CPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.IPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Retrieves partial patient data using Interface-based Projection.
     * <p>
     * IMPORTANT: The JPQL aliases (e.g., 'p.id AS id') MUST exactly match the
     * getter names in the IPatientInfo interface (e.g., 'getId()').
     * <p>
     * Spring Data JPA uses these aliases as keys in an internal 'TupleBackedMap'.
     * At runtime, a Dynamic Proxy is created for IPatientInfo; when a getter
     * is called, the proxy fetches the corresponding value from this map using
     * the alias as the lookup key.
     */
    @Query("select p.id as id, p.name as name, p.email as email from Patient p")
    List<IPatientInfo> getAllPatientInfo();


    /**
     * Spring Data JPA "Class-based Projection":
     * 'new' keyword is NOT required here because the method return type
     * is explicitly 'CPatientInfo'. Spring automatically maps the
     * selected fields (p.id, p.name) into the DTO's constructor
     * based on their position in the SELECT clause.
     * <p>
     * Standard JPQL (Without Spring Data) would require:
     *
     * @Query("select new com.shubh...dto.CPatientInfo(p.id, p.name) from Patient p")
     */
    @Query("select p.id , p.name from Patient p")
    List<CPatientInfo> getAllPatientInfoConcreteDTO();


    /**
     * Note: JPQL is case-sensitive for Java Identifiers (Entity 'Patient', field 'p.name')
     * but case-insensitive for JPQL Keywords (SELECT, FROM, WHERE).
     */

    @Query("select p.bloodGroup , count(p) from Patient p group by p.bloodGroup order by count(p) desc")
    List<BloodGroupStats> getBloodGroupStats();

    // Update Queries modify the database, they cannot execute
    // without having @Transactional and @Modifying Annotation
    @Query("update Patient p set p.name = :name where p.id in(:ids)")
    @Modifying
    @Transactional
    int updatePatientNameWithId(@Param("name") String name, @Param("ids") List<Integer> id);
}
