package com.shubh.module4.CurrencyConverter.service;

import com.shubh.module4.CurrencyConverter.dto.CurrencyConversionRequestDTO;
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

    public CurrencyConversionResponseDTO convertSourceToDestCurrencies(CurrencyConversionRequestDTO currencyConversionDTO) {
        CurrencyConversionResponseDTO conversionRateResponse = currencyConverterClient.get()
                .uri("?base_currency={currency}&currencies={currencies}",
                        currencyConversionDTO.getFromCurrency(),
                        currencyConversionDTO.getToCurrencies())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("4XX Client Error: " + new String(res.getBody().readAllBytes()));
                })
                .body(new ParameterizedTypeReference<CurrencyConversionResponseDTO>() {
                });
        BigDecimal units = currencyConversionDTO.getUnits();
        conversionRateResponse.getCurrencyConversions().values()
                .stream()
                .map(conversionRate -> conversionRate.multiply(units));

        conversionRateResponse.setSourceCurrency(currencyConversionDTO.getFromCurrency());
        conversionRateResponse.setUnits(units);
        return conversionRateResponse;
    }
}
