package com.Shubh.Module7.M7_TestingMethodologies.controller;

import com.Shubh.Module7.M7_TestingMethodologies.TestContainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Base blueprint for all integration tests.
 *
 * This class centralizes the common Spring Boot integration test
 * configuration (WebTestClient, Testcontainers, etc.), allowing concrete
 * integration test classes to inherit it instead of duplicating the same
 * setup. Subclasses only need to focus on the endpoint-specific test logic.
 */

// Automatically configures and exposes a WebTestClient bean for integration
// tests. Required in Spring Boot 4.x when using WebTestClient.
@AutoConfigureWebTestClient

// Starts the full application on a random HTTP port so WebTestClient can send
// real HTTP requests. The default MOCK environment does not start an embedded
// web server, so WebTestClient cannot connect to one.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
public abstract class BaseIntegrationTest {

    // Injected by @AutoConfigureWebTestClient. Declared as protected so all
    // integration test subclasses can directly use the configured
    // WebTestClient, even when they reside in different packages. A private
    // field would not be inherited, requiring every subclass to declare and
    // autowire its own WebTestClient.
    @Autowired
    protected WebTestClient webTestClient;
}