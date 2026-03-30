package com.shubh.JPATutorial.Module3_Projection_Hospital_Example;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.BloodGroupStats;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.CPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.IPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.PatientRepository;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest()
class PatientServiceTests {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientService patientService;

    @Test
    void testPatientProjectionInterface(){
//        List<Patient> patientList = patientRepository.findAll();
        // Get selective Patient information using dto interface
        List<IPatientInfo> patientList = patientRepository.getAllPatientInfo();
        for (IPatientInfo p : patientList){
            // shows reference instead of data directly in SpringBoot 4.x
//            System.out.println((p));
            System.out.println(p.getName() + " , " + p.getEmail() + " , " + p.getId());
        }
    }

    @Test
    void testPatientProjectionClass(){
        List<CPatientInfo> patientList = patientRepository.getAllPatientInfoConcreteDTO();
        for (CPatientInfo p : patientList){
            // works because we are not using proxy classes anymore
            // concrete class with tostring is defined
            System.out.println(p);
//            System.out.println(p.getName() + " , " + p.getEmail() + " , " + p.getId());
        }
    }

    @Test
    void testPatientCountByBloodGroupStats(){
        List<BloodGroupStats> bloodGroupStatsList = patientRepository.getBloodGroupStats();
        for (BloodGroupStats bloodGroupStatistic : bloodGroupStatsList){
            System.out.println(bloodGroupStatistic);
        }
    }

    @Test
    void testUpdatePatientNameMultipleRecords() {
        System.out.println("Updated : " + patientRepository.updatePatientNameWithId("Shubh",List.of(1,2)) + " records");
    }

    @Test
    void persistenceContextTestWithoutTransactional(){
        Patient patient = patientRepository.save(Patient.builder()
                .name("Shubh")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur123.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build()
        );
        // redundant db calls for the same record
        patientService.testPersistenceWithoutTransactional(patient.getId());
    }

    @Test
    void persistenceContextTestWithTransactional(){
        Patient patient = patientRepository.save(Patient.builder()
                .name("Shubh")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur123Transaction.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build()
        );
        patientService.testPersistenceWithTransactional(patient.getId());
    }

    @Test
    void dirtyEntityPersistedToDBTest(){
        Patient patient = patientRepository.save(Patient.builder()
                .name("Shubh")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur123DirtyTransaction.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build()
        );
        patientService.dirtyEntityPersistedToDB(patient.getId());
        // patient name commited to DB, verified by making selectDB call

        System.out.println(patientRepository.findById(patient.getId()));
    }
}
