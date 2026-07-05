package com.shubh.module5.Spring_Security_Practice.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class LoginDTO {
    String email;
    String password;
}

