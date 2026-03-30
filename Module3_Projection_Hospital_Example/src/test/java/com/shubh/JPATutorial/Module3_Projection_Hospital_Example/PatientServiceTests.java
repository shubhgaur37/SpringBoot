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
                .name("ShubhGAURRRRR")
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
                .name("ShubhGAUR")
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
                .name("ShubhGAURRR")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur123DirtyTransaction.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build()
        );
        patientService.dirtyEntityPersistedToDB(patient.getId());
        // patient name commited to DB, verified by making selectDB call

        System.out.println(patientRepository.findById(patient.getId()));
    }

    /**
     * N+1 Query Problem & LazyInitializationException Guide:
     *
     * 1. THE CAUSE (LazyInitializationException):
     *    This occurs when you try to access a LAZY collection after the Hibernate
     *    'Session' is closed. Repository methods like .findAll() open and close
     *    the session immediately. When the loop reaches System.out.println(patient),
     *    Lombok's toString() tries to read 'appointments', but there is no active
     *    database connection to fetch them.
     *
     * 2. THE IMPROPER FIX (FetchType.EAGER):
     *    Setting a collection to EAGER solves the exception but creates a permanent
     *    performance hit. Every single query for a Patient—even if you just need
     *    their name—will force a fetch of all their Appointments. This is the
     *    "Heavy" approach and should generally be avoided for collections.
     *
     * 3. THE PROPER FIX (Transactional Context):
     *    By adding @Transactional to this method, the Hibernate Session stays open
     *    for the entire duration of the test. When toString() triggers the lazy load,
     *    Hibernate can still reach the database.
     *
     * 4. THE OPTIMISED FIX (JOIN FETCH):
     *    Even with @Transactional, the code still fires N+1 queries. To get everything
     *    in exactly ONE query, use a "JOIN FETCH" in your Repository JPQL.
     */
    @Test
    void findAllPatientsWithAppointmentsNPLUSONE() {
        // Step 1: Initial query to fetch all patients (The "1" in N+1)
        List<Patient> patients = patientRepository.findAll();

        for (var patient : patients) {
            // Step 2: Accessing the patient.
            // If 'appointments' is LAZY, this triggers an extra query per patient (The "N").
            // If @Transactional is missing, this triggers the LazyInitializationException because the session closes
            // as soon as all patients are fetched, and we try to fire db queries on a non-existent session
            System.out.println("Patient: " + patient.getName());
            System.out.println("Appointments: " + patient.getAppointments());
        }
    }

    @Test
    void findAllPatientsWithAppointmentsSINGLEQUERY() {
        // fires a single query to fetch appointments for all patients. 1 DB call
        List<Patient> patients = patientRepository.getAllPatientsWithAppointments();

        for (var patient : patients){
            System.out.println(patient);
        }
    }

}
