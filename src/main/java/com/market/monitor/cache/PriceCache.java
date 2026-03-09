package com.market.monitor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Thin wrapper around the Caffeine "prices" cache.
 * Provides typed access and hides Spring Cache API from callers.
 */
@Component
@RequiredArgsConstructor
public class PriceCache {

    private final CacheManager cacheManager;

    private static final String CACHE_NAME = "prices";

    // ── Key builders ──────────────────────────────────────────────────────────

    public static String keyOf(AssetType type, String symbol) {
        return type.name() + ":" + symbol.toLowerCase();
    }

    // ── Write ──────────────────────────────────────────────────────────────────

    public void put(String key, MarketPriceDTO dto) {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).put(key, dto);
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    public Optional<MarketPriceDTO> get(String key) {
        var wrapper = Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).get(key);
        return Optional.ofNullable(wrapper != null ? (MarketPriceDTO) wrapper.get() : null);
    }

    /** Dump entire cache — used by the /market-data/all endpoint. */
    @SuppressWarnings("unchecked")
    public Map<String, MarketPriceDTO> getAll() {
        var springCache = Objects.requireNonNull(cacheManager.getCache(CACHE_NAME));
        Cache<Object, Object> native_ = (Cache<Object, Object>) springCache.getNativeCache();
        Map<String, MarketPriceDTO> result = new HashMap<>();
        native_.asMap().forEach((k, v) -> result.put(k.toString(), (MarketPriceDTO) v));
        return result;
    }
}