package com.shubh.module5.Spring_Security_Practice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class SignUpDTO {
    String name;
    String email;
    @JsonProperty("password")
    String passwordHash;
}
