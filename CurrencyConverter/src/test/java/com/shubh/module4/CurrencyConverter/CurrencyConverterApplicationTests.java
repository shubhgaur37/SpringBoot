package com.shubh.module4.CurrencyConverter;

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
		System.out.println(currencyConverterService.convertSourceToDestCurrencies("INR","AUD,USD",BigDecimal.valueOf(100000)));
	}
}
