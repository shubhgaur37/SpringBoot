# Currency Converter

A mini Spring Boot REST API that converts an amount from one source currency into one or more destination currencies. The project uses `freecurrencyapi.com` as the external exchange-rate provider and exposes a simple endpoint for clients.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring RestClient
- Lombok
- Maven
- Springdoc OpenAPI / Swagger UI

## Project Structure

```text
src/main/java/com/shubh/module4/CurrencyConverter
├── CurrencyConverterApplication.java
├── advice
│   ├── ApiError.java
│   └── GlobalErrorHandler.java
├── config
│   ├── AppConfig.java
│   └── RestClientConfig.java
├── controller
│   └── CurrencyConverterController.java
├── dto
│   └── CurrencyConversionResponseDTO.java
└── service
    └── CurrencyConverterService.java
```

## Configuration

The application expects configuration in `src/main/resources/application.yaml`.

To use the project, register on `freecurrencyapi.com`, generate an API key, and add that key in the YAML file. The committed YAML file does not include a personal API key, so each user must provide their own key for the app to work.

No extra configuration mechanism has been added in this project; update the YAML file with the required values before running the app.

Example `application.yaml`:

```yaml
currencyConvertor:
  base:
    url: https://api.freecurrencyapi.com/v1/latest

  api:
    key: YOUR_FREECURRENCY_API_KEY

spring:
  application:
    name: CurrencyConverter
```

Required properties:

- `currencyConvertor.base.url`: Base URL of the external currency API.
- `currencyConvertor.api.key`: API key generated from `freecurrencyapi.com`.

## How It Works

1. The client calls the local `/convertCurrency` endpoint.
2. `CurrencyConverterController` accepts the source currency, destination currencies, and amount.
3. `CurrencyConverterService` calls the configured external currency API using Spring `RestClient`.
4. The external API returns conversion rates.
5. The service multiplies each conversion rate by the requested units.
6. The API returns the converted values in the response.

## Run the Application

Make sure Java 21 is installed, then run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

By default, the application runs at:

```text
http://localhost:8080
```

## API Endpoint

### Convert Currency

```http
GET /convertCurrency
```

Query parameters:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `srcCurrency` | String | Yes | Source currency code, for example `USD` |
| `destCurrencies` | String | No | Comma-separated destination currency codes, for example `INR,EUR,GBP`. If not passed, conversions for all available currencies are returned. |
| `units` | BigDecimal | Yes | Amount of source currency to convert |

Example request with selected destination currencies:

```bash
curl "http://localhost:8080/convertCurrency?srcCurrency=USD&destCurrencies=INR,EUR&units=10"
```

Example request for all available destination currencies:

```bash
curl "http://localhost:8080/convertCurrency?srcCurrency=USD&units=10"
```

Example response:

```json
{
  "sourceCurrency": "USD",
  "units": 10,
  "data": {
    "INR": 834.25,
    "EUR": 9.27
  }
}
```

The exact values depend on the latest exchange rates returned by the external API.

## Swagger UI

Swagger UI is available through Springdoc OpenAPI after the application starts.

Open this URL in your browser:

```text
http://localhost:8080/swagger-ui/index.html
```

Use Swagger to make a request:

1. Start the application.
2. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.
3. Expand the `currency-converter-controller` section.
4. Open `GET /convertCurrency`.
5. Click `Try it out`.
6. Enter values for:
   - `srcCurrency`, for example `USD`
   - `destCurrencies`, for example `INR,EUR`; leave it empty to get conversions for all available currencies
   - `units`, for example `10`
7. Click `Execute`.
8. Review the response body and status code in Swagger UI.

## Error Handling

The app has a global runtime exception handler. If the external API returns a client error or server error, the application returns a `400 BAD_REQUEST` response with an error body like:

```json
{
  "timeStamp": "2026-06-27T00:00:00",
  "message": "error message"
}
```

## Tests

Run tests with:

```bash
./mvnw test
```

## Notes

- Currency codes should be valid ISO-style currency codes supported by the external API.
- `destCurrencies` can be passed as a comma-separated string. If it is not provided, the response includes conversions for all available currencies.
- Register on `freecurrencyapi.com` and generate your own API key before running the app.
