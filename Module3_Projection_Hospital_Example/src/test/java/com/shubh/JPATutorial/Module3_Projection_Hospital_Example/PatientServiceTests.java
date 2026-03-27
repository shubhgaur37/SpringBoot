package com.shubh.JPATutorial.Module3_Projection_Hospital_Example;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.BloodGroupStats;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.CPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.dto.IPatientInfo;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PatientServiceTests {

    @Autowired
    private PatientRepository patientRepository;

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
}
