package com.market.monitor.controller;

import com.market.monitor.cache.PriceCache;
import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.AssetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Query cached market prices")
public class MarketDataController {

    private final PriceCache priceCache;

    @GetMapping
    @Operation(summary = "Get latest cached price for a symbol",
               description = "Example: GET /api/v1/market-data?symbol=bitcoin&type=CRYPTO")
    public ResponseEntity<MarketPriceDTO> getPrice(@RequestParam String symbol,
                                                   @RequestParam AssetType type) {
        String key = PriceCache.keyOf(type, symbol);
        return priceCache.get(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @Operation(summary = "Dump all prices currently held in the Caffeine cache")
    public Map<String, MarketPriceDTO> getAll() {
        return priceCache.getAll();
    }
}