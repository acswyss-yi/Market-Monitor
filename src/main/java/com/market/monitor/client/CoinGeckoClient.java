package com.market.monitor.client;

import com.market.monitor.exception.ExternalApiException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Fetches cryptocurrency prices from CoinGecko.
 *
 * <p>Symbol convention: use CoinGecko IDs directly, e.g. "bitcoin", "ethereum", "solana".
 * Set COIN_GECKO_API_KEY env var to use a Demo API key.
 */
@Component
@Log4j2
public class CoinGeckoClient {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";

    private final RestClient restClient;
    private final String apiKey;

    public CoinGeckoClient(RestClient restClient,
                           @Value("${app.coin-gecko.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    /**
     * Batch-fetch USD prices for the given CoinGecko IDs.
     *
     * @return map of symbol → price (USD)
     */
    public Map<String, BigDecimal> fetchPrices(List<String> symbols) {
        String ids = String.join(",", symbols);
        String uri = BASE_URL + "/simple/price?ids={ids}&vs_currencies=usd"
                + (apiKey.isBlank() ? "" : "&x_cg_demo_api_key=" + apiKey);

        log.debug("CoinGecko request: ids={}", ids);

        try {
            Map<String, Map<String, Object>> response = restClient.get()
                    .uri(uri, ids)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) return Map.of();

            return response.entrySet().stream()
                    .filter(e -> e.getValue().containsKey("usd"))
                    .collect(
                            java.util.stream.Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> new BigDecimal(e.getValue().get("usd").toString())
                            )
                    );

        } catch (Exception e) {
            log.error("CoinGecko API error: {}", e.getMessage());
            throw new ExternalApiException("CoinGecko", e.getMessage());
        }
    }
}