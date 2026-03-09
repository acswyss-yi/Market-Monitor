package com.market.monitor.client;

import com.market.monitor.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches stock prices from Alpha Vantage (free API key, 5 req/min limit).
 *
 * <p>Register a free key at https://www.alphavantage.co/support/#api-key
 * Set via environment variable: ALPHA_VANTAGE_API_KEY=your_key
 * The placeholder key "demo" works only for symbol "IBM".
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class AlphaVantageClient {

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final RestClient restClient;

    @Value("${app.alpha-vantage.api-key:demo}")
    private String apiKey;

    /**
     * Fetch the latest price for a single stock ticker (e.g. "AAPL", "TSLA").
     *
     * @return price wrapped in Optional; empty if the API returns no data
     */
    @SuppressWarnings("unchecked")
    public Optional<BigDecimal> fetchStockPrice(String symbol) {
        log.debug("AlphaVantage request: symbol={}", symbol);

        try {
            Map<String, Object> response = restClient.get()
                    .uri(BASE_URL + "?function=GLOBAL_QUOTE&symbol={symbol}&apikey={key}",
                            symbol, apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) return Optional.empty();

            Map<String, String> quote = (Map<String, String>) response.get("Global Quote");
            if (quote == null || !quote.containsKey("05. price")) {
                log.warn("AlphaVantage returned no data for symbol={}", symbol);
                return Optional.empty();
            }

            return Optional.of(new BigDecimal(quote.get("05. price")));

        } catch (Exception e) {
            log.error("AlphaVantage API error for symbol={}: {}", symbol, e.getMessage());
            throw new ExternalApiException("AlphaVantage", e.getMessage());
        }
    }
}