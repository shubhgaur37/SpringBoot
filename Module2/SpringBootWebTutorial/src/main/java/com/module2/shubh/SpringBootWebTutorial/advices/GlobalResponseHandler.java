package com.module2.shubh.SpringBootWebTutorial.advices;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// defining this handler by implementing ResponseBodyAdvice ensures that
// response dto's data comes inside data field of api response
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {
    // the first parameter represents the type of dto
    // we can define what kind of dtos are eligible for conversion
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // for now we are supporting conversion for all the dto's
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse<?>)
            return body;
        // convert to api response body for any other type of body
        return new ApiResponse<>(body);
    }
}
