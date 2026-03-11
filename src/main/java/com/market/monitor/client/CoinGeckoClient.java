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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fetches cryptocurrency prices from Gate.io — accessible from mainland China.
 *
 * <p>Symbol convention: use CoinGecko IDs, e.g. "bitcoin", "ethereum", "solana".
 * They are mapped internally to Gate.io currency pairs (BTC_USDT, ETH_USDT, etc.).
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class CoinGeckoClient {

    private static final String GATE_URL = "https://api.gateio.ws/api/v4/spot/tickers";

    /** CoinGecko ID → Gate.io currency_pair */
    private static final Map<String, String> ID_TO_GATE = Map.ofEntries(
            Map.entry("bitcoin",       "BTC_USDT"),
            Map.entry("ethereum",      "ETH_USDT"),
            Map.entry("solana",        "SOL_USDT"),
            Map.entry("binancecoin",   "BNB_USDT"),
            Map.entry("ripple",        "XRP_USDT"),
            Map.entry("cardano",       "ADA_USDT"),
            Map.entry("dogecoin",      "DOGE_USDT"),
            Map.entry("polkadot",      "DOT_USDT"),
            Map.entry("avalanche-2",   "AVAX_USDT"),
            Map.entry("chainlink",     "LINK_USDT"),
            Map.entry("uniswap",       "UNI_USDT"),
            Map.entry("litecoin",      "LTC_USDT"),
            Map.entry("shiba-inu",     "SHIB_USDT"),
            Map.entry("matic-network", "MATIC_USDT"),
            Map.entry("tron",          "TRX_USDT"),
            Map.entry("stellar",       "XLM_USDT"),
            Map.entry("monero",        "XMR_USDT"),
            Map.entry("toncoin",       "TON_USDT"),
            Map.entry("pepe",          "PEPE_USDT"),
            Map.entry("sui",           "SUI_USDT")
    );

    private final RestClient restClient;

    /**
     * Batch-fetch USD prices for the given CoinGecko IDs via Gate.io API.
     *
     * @return map of CoinGecko ID → price (USD)
     */
    public Map<String, BigDecimal> fetchPrices(List<String> symbols) {
        // Gate.io currency pairs we need
        Map<String, String> gateToId = symbols.stream()
                .filter(ID_TO_GATE::containsKey)
                .collect(Collectors.toMap(ID_TO_GATE::get, id -> id));

        if (gateToId.isEmpty()) {
            log.warn("No Gate.io mapping found for symbols: {}", symbols);
            return Map.of();
        }

        Set<String> wanted = gateToId.keySet();
        log.debug("Gate.io request: symbols={}", wanted);

        try {
            // Response: [ { "currency_pair": "BTC_USDT", "last": "50000.0", ... } ]
            List<Map<String, Object>> data = restClient.get()
                    .uri(GATE_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (data == null) {
                log.warn("Gate.io returned null response");
                return Map.of();
            }

            return data.stream()
                    .filter(e -> wanted.contains(e.get("currency_pair")))
                    .collect(Collectors.toMap(
                            e -> gateToId.get(e.get("currency_pair")),
                            e -> new BigDecimal(e.get("last").toString())
                    ));

        } catch (Exception e) {
            log.error("Gate.io API error: {}", e.getMessage());
            throw new ExternalApiException("CoinGecko", e.getMessage());
        }
    }
}
