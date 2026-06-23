package com.shubh.module4.Prod_Ready_Features.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    String errorMessage;
}
