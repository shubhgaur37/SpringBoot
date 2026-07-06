package com.shubh.module5.Spring_Security_Demo.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponseDTO {
    String accessToken;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String refreshToken;
}
