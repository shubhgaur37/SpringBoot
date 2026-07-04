package com.shubh.module5.Spring_Security_Demo.advice;

import com.shubh.module5.Spring_Security_Demo.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception) {
        ApiError apiError = new ApiError(exception.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    // Handling Authentication Exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException exception) {
        // Spring Security defaults to 403 forbidden error for all exceptions if not handled
        ApiError apiError = new ApiError(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }

    /**
     * HandlerExceptionResolver is Spring MVC's central exception resolution mechanism.
     * <p>
     * During application startup, Spring scans every @ControllerAdvice /
     *
     * @RestControllerAdvice bean and discovers all @ExceptionHandler methods.
     * It caches a mapping between exception types and their corresponding
     * handler methods.
     * <p>
     * Calling resolveException(...) delegates an exception to this cached
     * resolution mechanism. HandlerExceptionResolver finds the most specific
     * matching @ExceptionHandler, invokes it, and writes the returned
     * ResponseEntity to the HTTP response.
     * <p>
     * <p>
     * This is especially useful inside filters, where exceptions occur before
     * <p>
     * DispatcherServlet is reached and therefore cannot be handled
     * <p>
     * automatically by @RestControllerAdvice. By delegating the exception to
     * <p>
     * HandlerExceptionResolver, the exception is processed exactly the same way
     * <p>
     * as one thrown from a controller.
     */
    // Handling JWT Exception
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwtExceptions(JwtException exception) {
        // Spring Security defaults to 403 forbidden error for all exceptions if not handled
        ApiError apiError = new ApiError(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(apiError, apiError.getStatusCode());
    }
}
