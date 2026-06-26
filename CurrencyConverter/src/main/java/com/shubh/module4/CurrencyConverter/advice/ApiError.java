package com.shubh.module4.CurrencyConverter.advice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class ApiError {
    LocalDateTime timeStamp;
    String message;

    ApiError() {
        timeStamp = LocalDateTime.now();
    }

    ApiError(String message) {
        this();
        this.message = message;
    }
}
