package com.shubh.module4.Prod_Ready_Features.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
public class RestClientConfig {

    @Value("${employeeService.base.url}")
    private String BASE_URL;

    @Bean
    /* * 💡 BEAN QUALIFICATION:
     * Assigns a unique identifier to this specific RestClient bean instance.
     * This eliminates ambiguity and prevents injection conflicts when multiple distinct
     * RestClient beans are registered within the Spring Application Context.
     */
    @Qualifier("employeeRestClient")
    RestClient getEmployeeServiceRestClient() {
        /* * 💡 SYNCHRONOUS / BLOCKING NATURE:
         * By default, RestClient executes network requests synchronously using a blocking I/O model.
         * When an outbound call is dispatched, the executing thread drops into a paused (blocked) state,
         * sitting entirely idle while waiting for the remote server to process the request and return
         * the HTTP response.
         *
         * 🚀 SCALABILITY NOTE:
         * While blocking calls can bottleneck traditional platform thread pools (like standard Tomcat pools),
         * this model becomes exceptionally scalable when paired with Java 21+ Virtual Threads (Project Loom).
         * Virtual threads allow the underlying physical carrier thread to be mounted out to do other work
         * while this RestClient waits on network I/O.
         */
        return RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
    }
}