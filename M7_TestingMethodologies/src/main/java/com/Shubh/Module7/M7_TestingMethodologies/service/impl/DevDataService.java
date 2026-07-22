package com.Shubh.Module7.M7_TestingMethodologies.service.impl;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


/*
 * Why did @DataJpaTest fail during Maven packaging?
 *
 * Although the DEV profile was active via @ActiveProfiles("DEV"), @DataJpaTest
 * creates only a JPA slice of the application context. Service beans are
 * excluded from this slice.
 *
 * The @SpringBootApplication class had a constructor dependency on
 * DataService. During context initialization, Spring attempted to instantiate
 * the application class, but no DataService bean existed in the JPA test
 * context, resulting in an UnsatisfiedDependencyException before any repository
 * tests could execute.
 */


@Service
@Profile("DEV")
public class DevDataService implements DataService {

    @Override
    public String getData() {
        return "DEV_STAGING_DATA";
    }

    @Override
    public String getEnvironment() {
        return "DEV_STAGING";
    }
}

/*
@DataJpaTest Context Initialization

@DataJpaTest
        │
        ▼
Locate @SpringBootConfiguration
(M7TestingMethodologiesApplication)
        │
        ▼
Create JPA slice ApplicationContext
(Repositories, EntityManager, DataSource, etc.)
        │
        ▼
Exclude non-JPA components
(@Service, @Controller, @Component, ...)
        │
        ▼
Instantiate M7TestingMethodologiesApplication
        │
        ▼
Constructor requires DataService
        │
        ▼
Search for DataService bean
        │
        ▼
No matching bean in JPA slice
        │
        ▼
UnsatisfiedDependencyException
        │
        ▼
ApplicationContext fails to start
        │
        ▼
Repository tests are never executed
*/
