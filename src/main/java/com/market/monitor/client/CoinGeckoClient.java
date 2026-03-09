package com.market.monitor.client;

import com.market.monitor.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches cryptocurrency prices from CoinGecko (free tier, no API key required).
 *
 * <p>Symbol convention: use CoinGecko IDs directly, e.g. "bitcoin", "ethereum", "solana".
 * See https://api.coingecko.com/api/v3/coins/list for the full list.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class CoinGeckoClient {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";

    private final RestClient restClient;

    /**
     * Batch-fetch USD prices for the given CoinGecko IDs.
     *
     * @return map of symbol → price (USD)
     */
    public Map<String, BigDecimal> fetchPrices(List<String> symbols) {
        String ids = String.join(",", symbols);
        String uri = BASE_URL + "/simple/price?ids={ids}&vs_currencies=usd";

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