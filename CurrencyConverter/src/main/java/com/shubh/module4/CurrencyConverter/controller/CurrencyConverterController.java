package com.shubh.module4.CurrencyConverter.controller;

import com.shubh.module4.CurrencyConverter.dto.CurrencyConversionResponseDTO;
import com.shubh.module4.CurrencyConverter.service.CurrencyConverterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RestController
public class CurrencyConverterController {
    CurrencyConverterService currencyConverterService;

    /**
     *
     * @param srcCurrency
     * @param destCurrencies: comma separated list of currencies
     * @param units
     * @returns conversion value of src to dest currency based on specified units
     */
    @GetMapping(path = "/convertCurrency")
    public ResponseEntity<CurrencyConversionResponseDTO> getConversion(@RequestParam String srcCurrency,
                                                                       @RequestParam(defaultValue = "") String destCurrencies,
                                                                       @RequestParam BigDecimal units) {
        return ResponseEntity.ok(currencyConverterService.convertSourceToDestCurrencies(srcCurrency, destCurrencies, units));
    }
}
