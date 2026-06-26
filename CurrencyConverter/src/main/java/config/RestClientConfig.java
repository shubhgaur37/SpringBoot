package config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("currencyConvertor.base.url")
    private String BASE_URL;

    @Value("currencyConvertor.api.key")
    private String apiKey;

    @Bean
    public RestClient getCurrencyConvertorRestClient() {
        return RestClient.builder()
                .baseUrl(BASE_URL + apiKey)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Internal Server Error: " + new String(res.getBody().readAllBytes()));
                }).build();
    }

}
