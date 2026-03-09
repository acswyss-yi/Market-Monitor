package com.market.monitor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.price-ttl-minutes:10}")
    private int priceTtlMinutes;

    /**
     * Caffeine-backed CacheManager.
     * Cache "prices" holds MarketPriceDTO keyed by "CRYPTO:bitcoin" or "STOCK:AAPL".
     * TTL ensures stale prices are evicted even if schedulers stop running.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("prices");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(priceTtlMinutes, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()
        );
        return manager;
    }
}