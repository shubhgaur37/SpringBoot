package com.shubh.module4.CurrencyConverter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CurrencyConversionRequestDTO {
    String fromCurrency;
    String toCurrencies;
    BigDecimal units;
}
