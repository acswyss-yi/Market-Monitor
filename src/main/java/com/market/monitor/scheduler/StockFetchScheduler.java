package com.market.monitor.scheduler;

import com.market.monitor.cache.PriceCache;
import com.market.monitor.client.AlphaVantageClient;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cycles through stock symbols one at a time to respect Alpha Vantage's
 * free-tier rate limit (5 requests/minute). One symbol is fetched every
 * 15 seconds by default — safe for up to 4 watched symbols simultaneously.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class StockFetchScheduler {

    private final AlertRuleMapper alertRuleMapper;
    private final AlphaVantageClient alphaVantageClient;
    private final PriceCache priceCache;
    private final AlertEvaluationService alertEvaluationService;

    /** Rotating index to pick the next symbol to fetch. */
    private final AtomicInteger cursor = new AtomicInteger(0);

    @Scheduled(fixedRateString = "${app.scheduler.stock-interval-ms:15000}")
    public void fetchOneAndEvaluate() {
        var rules = alertRuleMapper.findAllEnabled();
        List<String> symbols = rules.stream()
                .filter(r -> r.getAssetType() == AssetType.STOCK)
                .map(r -> r.getSymbol().toUpperCase())
                .distinct()
                .toList();

        if (symbols.isEmpty()) {
            log.debug("No enabled STOCK rules — skipping Alpha Vantage fetch");
            return;
        }

        String symbol = symbols.get(cursor.getAndIncrement() % symbols.size());
        log.info("Fetching STOCK price for: {}", symbol);

        alphaVantageClient.fetchStockPrice(symbol).ifPresent(price -> {
            String key = PriceCache.keyOf(AssetType.STOCK, symbol);
            priceCache.put(key, new MarketPriceDTO(symbol, AssetType.STOCK, price, LocalDateTime.now()));
            log.info("Cached STOCK {} = ${}", symbol, price);
            alertEvaluationService.evaluate(rules);
        });
    }
}