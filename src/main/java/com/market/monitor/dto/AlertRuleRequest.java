package com.market.monitor.dto;

import com.market.monitor.enums.AssetType;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.enums.NotifyChannel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Condition type is required")
    private ConditionType conditionType;

    @NotNull(message = "Threshold price is required")
    @Positive(message = "Threshold price must be positive")
    private BigDecimal thresholdPrice;

    @NotNull(message = "Notify channel is required")
    private NotifyChannel notifyChannel;

    @NotBlank(message = "Notify target (email or webhook URL) is required")
    private String notifyTarget;

    /** Optional, defaults to 60 minutes */
    @Min(value = 1, message = "Cooldown must be at least 1 minute")
    private Integer cooldownMinutes;
}