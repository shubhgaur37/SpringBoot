package com.shubh.module4.CurrencyConverter.service;

import com.shubh.module4.CurrencyConverter.dto.CurrencyConversionResponseDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class CurrencyConverterService {
    RestClient currencyConverterClient;

    public CurrencyConversionResponseDTO convertSourceToDestCurrencies(String srcCurrency, String destCurrencies, BigDecimal units) {
        CurrencyConversionResponseDTO conversionRateResponse = currencyConverterClient.get()
                .uri("?base_currency={currency}&currencies={currencies}",
                        srcCurrency,
                        destCurrencies)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("4XX Client Error: " + new String(res.getBody().readAllBytes()));
                })
                .body(new ParameterizedTypeReference<CurrencyConversionResponseDTO>() {
                });

        conversionRateResponse.getCurrencyConversions().replaceAll((currency, rate) -> rate.multiply(units));
        conversionRateResponse.setSourceCurrency(srcCurrency);
        conversionRateResponse.setUnits(units);
        return conversionRateResponse;
    }
}
