package com.shubh.module4.CurrencyConverter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrencyConversionResponseDTO {
    String sourceCurrency;
    BigDecimal units;
    @JsonProperty("data")
    Map<String, BigDecimal> currencyConversions;
}
