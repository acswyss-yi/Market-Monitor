package com.market.monitor.dto;

import com.market.monitor.enums.AssetType;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.enums.NotifyChannel;
import com.market.monitor.model.AlertRule;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AlertRuleResponse {

    private Long id;
    private String name;
    private AssetType assetType;
    private String symbol;
    private ConditionType conditionType;
    private BigDecimal thresholdPrice;
    private NotifyChannel notifyChannel;
    private String notifyTarget;
    private boolean enabled;
    private int cooldownMinutes;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AlertRuleResponse from(AlertRule rule) {
        return AlertRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .assetType(rule.getAssetType())
                .symbol(rule.getSymbol())
                .conditionType(rule.getConditionType())
                .thresholdPrice(rule.getThresholdPrice())
                .notifyChannel(rule.getNotifyChannel())
                .notifyTarget(rule.getNotifyTarget())
                .enabled(rule.isEnabled())
                .cooldownMinutes(rule.getCooldownMinutes())
                .lastTriggeredAt(rule.getLastTriggeredAt())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}