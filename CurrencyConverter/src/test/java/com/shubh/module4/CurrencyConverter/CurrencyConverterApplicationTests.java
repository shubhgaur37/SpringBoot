package com.shubh.module4.CurrencyConverter;

import com.shubh.module4.CurrencyConverter.dto.CurrencyConversionRequestDTO;
import com.shubh.module4.CurrencyConverter.service.CurrencyConverterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class CurrencyConverterApplicationTests {

	@Autowired
	private CurrencyConverterService currencyConverterService;

	@Test
	void testCurrencyConversionFromUSDToINR(){
		CurrencyConversionRequestDTO request = new CurrencyConversionRequestDTO("INR","USD,INR,AUD",new BigDecimal("10001"));
		System.out.println(currencyConverterService.convertSourceToDestCurrencies(request));
	}
}
