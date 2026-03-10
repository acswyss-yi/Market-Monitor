package com.market.monitor.scheduler;

import com.market.monitor.cache.PriceCache;
import com.market.monitor.client.CoinGeckoClient;
import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.AssetType;
import com.market.monitor.mapper.AlertRuleMapper;
import com.market.monitor.service.AlertEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Periodically pulls crypto prices from CoinGecko for all symbols
 * referenced by enabled alert rules, then triggers alert evaluation.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class CryptoFetchScheduler {

    private final AlertRuleMapper alertRuleMapper;
    private final CoinGeckoClient coinGeckoClient;
    private final PriceCache priceCache;
    private final AlertEvaluationService alertEvaluationService;

    @Scheduled(fixedRateString = "${app.scheduler.crypto-interval-ms:300000}")
    public void fetchAndEvaluate() {
        var rules = alertRuleMapper.findAllEnabled();
        List<String> symbols = rules.stream()
                .filter(r -> r.getAssetType() == AssetType.CRYPTO)
                .map(r -> r.getSymbol().toLowerCase())
                .distinct()
                .toList();

        if (symbols.isEmpty()) {
            log.debug("No enabled CRYPTO rules — skipping CoinGecko fetch");
            return;
        }

        log.info("Fetching CRYPTO prices for: {}", symbols);
        Map<String, java.math.BigDecimal> prices = coinGeckoClient.fetchPrices(symbols);

        prices.forEach((symbol, price) -> {
            String key = PriceCache.keyOf(AssetType.CRYPTO, symbol);
            priceCache.put(key, new MarketPriceDTO(symbol, AssetType.CRYPTO, price, LocalDateTime.now()));
            log.info("Cached CRYPTO {} = ${}", symbol, price);
        });

        alertEvaluationService.evaluate(rules);
    }
}