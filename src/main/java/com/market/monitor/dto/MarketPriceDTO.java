package com.market.monitor.dto;

import com.market.monitor.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPriceDTO {

    private String symbol;
    private AssetType assetType;
    private BigDecimal price;
    private LocalDateTime fetchedAt;
}