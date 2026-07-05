package com.shubh.module5.Spring_Security_Practice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


// Spring Boot auto-detects Filter beans (@Component + OncePerRequestFilter)
// and registers them with the Servlet container automatically.
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * =========================================================================
         * HTTP SERVLET REQUEST & RESPONSE
         * =========================================================================
         *
         * HttpServletRequest and HttpServletResponse are Servlet API abstractions
         * representing an incoming HTTP request and the outgoing HTTP response.
         *
         * The Servlet Container (Tomcat, Jetty, Undertow, etc.) creates these
         * objects for every request and passes them through the Servlet Filter
         * Chain before the request eventually reaches the DispatcherServlet and
         * your controllers.
         *
         * Request Lifecycle:
         *
         * Client
         *    │
         *    ▼
         * Servlet Container
         *    │
         *    ▼
         * Filter Chain
         *    │
         *    ▼
         * DispatcherServlet
         *    │
         *    ▼
         * Controller
         */

        /*
         * =========================================================================
         * BASIC REQUEST METADATA
         * =========================================================================
         *
         * These methods simply expose metadata about the request and are safe to
         * call multiple times. They DO NOT read or consume the request body.
         */
        log.info(
                "Incoming Request -> Method: {} ; URI: {} ; Query Params: {} ; Client IP: {}",
                request.getMethod(),          // GET, POST, PUT, DELETE...
                request.getRequestURI(),      // Endpoint being accessed along with Path Params
                request.getQueryString(),     // Query parameters (if present)
                request.getRemoteAddr()       // IP address of the client
        );

        /*
         * =========================================================================
         * WHY DO WE NEED CONTENT CACHING WRAPPERS?
         * =========================================================================
         *
         * Sending or receiving an HTTP request is a Network I/O operation.
         *
         * I/O (Input / Output) refers to communication with resources outside
         * the application's memory such as:
         *
         *  - Files
         *  - Databases
         *  - Network connections (HTTP, TCP)
         *  - Kafka
         *  - Redis
         *
         * An HTTP request travels over a TCP connection as a stream of bytes.
         *
         * Servlet API exposes the request body through:
         *
         *      request.getInputStream()
         *
         * and the response body through:
         *
         *      response.getOutputStream()
         *
         * These are stream-based APIs.
         *
         * Streams are forward-only and intended to be consumed once.
         * Every read advances the cursor until EOF (End Of File).
         *
         * Therefore:
         *
         *      request.getInputStream().readAllBytes();
         *
         * consumes the request body.
         *
         * Later, Spring MVC (Jackson's HttpMessageConverter) also attempts to
         * read this InputStream while converting @RequestBody into a Java object.
         *
         * If the stream has already been consumed inside this filter,
         * the controller receives an empty body and request deserialization fails.
         *
         * Similarly, the response body is written once to an OutputStream while
         * returning data to the client.
         *
         * To safely inspect request/response payloads without consuming the
         * original streams, Spring provides:
         *
         *      ContentCachingRequestWrapper
         *      ContentCachingResponseWrapper
         *
         * These classes extend HttpServletRequestWrapper and
         * HttpServletResponseWrapper respectively (Decorator Pattern).
         *
         * They intercept reads/writes, cache the bytes internally,
         * and allow the payload to be inspected after request processing
         * has completed.
         */

        // Cache Limit protects the memory from exhaustion, possible that large files are sent as payh
        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request, 1024 * 1024); // Cache up to 1 MB of request

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {

            /*
             * Continue processing using the WRAPPED request and response.
             *
             * This is extremely important.
             *
             * If the original request/response objects were passed instead,
             * the wrappers would never observe the request body being read
             * or the response body being written.
             *
             * By passing the wrappers into the filter chain:
             *
             *      filterChain
             *          │
             *          ▼
             * DispatcherServlet
             *          │
             *          ▼
             * Controller
             *
             * every downstream component (Spring MVC, Jackson, Controllers,
             * Exception Handlers, etc.) interacts with the wrappers instead of
             * the original servlet objects.
             *
             * When Jackson reads request.getInputStream(), it is actually reading
             * from ContentCachingRequestWrapper.
             *
             * The wrapper delegates the read to the original request while
             * simultaneously caching every byte that passes through.
             *
             * Likewise, when the controller writes the response,
             * ContentCachingResponseWrapper intercepts those writes and stores
             * a copy of the response body.
             */
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {

            /*
             * finally guarantees that response logging executes even when an
             * exception is thrown during request processing.
             */
            long timeTaken = System.currentTimeMillis() - startTime;

            /*
             * At this point:
             *
             * - Jackson has already consumed the request body.
             * - The controller has already generated the response.
             *
             * Since both wrappers cached the data during processing,
             * we can safely retrieve the payloads without consuming
             * the original streams again.
             */
            String requestBody = new String(
                    requestWrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8
            );

            String responseBody = new String(
                    responseWrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8
            );

            log.debug(
                    "Outgoing Response -> StatusCode: {} ; Time Taken: {} ms",
                    responseWrapper.getStatus(),
                    timeTaken
            );

            /*
             * Payload logging should generally be DEBUG level because payloads
             * can be large and may contain sensitive information.
             *
             * In production systems, fields such as passwords, JWTs,
             * Authorization headers, API keys, OTPs, etc. should be masked
             * before logging or omitted entirely.
             */
            log.trace("Request Payload  -> {}", requestBody);
            log.trace("Response Payload -> {}", responseBody);

            /*
             * IMPORTANT:
             *
             * ContentCachingResponseWrapper buffers the response internally.
             *
             * Unless the cached response is copied back to the original
             * HttpServletResponse, the client will receive an empty response
             * body.
             *
             * Always invoke copyBodyToResponse() after reading the cached
             * response payload.
             */
            responseWrapper.copyBodyToResponse();
        }
    }
}

