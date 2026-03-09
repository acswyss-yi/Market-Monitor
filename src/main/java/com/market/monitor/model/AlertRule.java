package com.market.monitor.model;

import com.market.monitor.enums.AssetType;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.enums.NotifyChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Plain POJO mapped by MyBatis — no JPA annotations needed.
 * Switch to MySQL: only change datasource config, this class stays the same.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    private Long id;

    /** Human-readable rule name, e.g. "BTC跌破6万预警" */
    private String name;

    private AssetType assetType;

    /** CoinGecko ID for CRYPTO (e.g. "bitcoin"), ticker for STOCK (e.g. "AAPL") */
    private String symbol;

    private ConditionType conditionType;

    private BigDecimal thresholdPrice;

    private NotifyChannel notifyChannel;

    /** Email address or DingTalk webhook URL */
    private String notifyTarget;

    private boolean enabled;

    /** Minimum minutes between two consecutive notifications for this rule */
    private int cooldownMinutes;

    private LocalDateTime lastTriggeredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}