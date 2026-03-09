package com.market.monitor.service;

import com.market.monitor.cache.PriceCache;
import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.AssetType;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.mapper.AlertRuleMapper;
import com.market.monitor.model.AlertRule;
import com.market.monitor.notification.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Core alert evaluation logic.
 * Called by both schedulers after each price fetch.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class AlertEvaluationService {

    private final AlertRuleMapper alertRuleMapper;
    private final PriceCache priceCache;
    private final NotificationRouter notificationRouter;

    public void evaluate() {
        List<AlertRule> rules = alertRuleMapper.findAllEnabled();
        log.debug("Evaluating {} enabled alert rule(s)", rules.size());

        for (AlertRule rule : rules) {
            try {
                evaluateOne(rule);
            } catch (Exception e) {
                log.error("Error evaluating rule[{}]: {}", rule.getId(), e.getMessage());
            }
        }
    }

    private void evaluateOne(AlertRule rule) {
        String key = PriceCache.keyOf(rule.getAssetType(), rule.getSymbol());
        Optional<MarketPriceDTO> priceOpt = priceCache.get(key);

        if (priceOpt.isEmpty()) {
            log.debug("No cached price for rule[{}] symbol={}", rule.getId(), rule.getSymbol());
            return;
        }

        MarketPriceDTO price = priceOpt.get();
        boolean triggered = switch (rule.getConditionType()) {
            case ABOVE -> price.getPrice().compareTo(rule.getThresholdPrice()) > 0;
            case BELOW -> price.getPrice().compareTo(rule.getThresholdPrice()) < 0;
        };

        if (triggered && shouldNotify(rule)) {
            log.info("Alert TRIGGERED — rule[{}] '{}': {} {} {} (current: ${})",
                    rule.getId(), rule.getName(), rule.getSymbol(),
                    rule.getConditionType(), rule.getThresholdPrice(), price.getPrice());

            notificationRouter.route(rule, price);
            alertRuleMapper.updateLastTriggeredAt(rule.getId(), LocalDateTime.now());
        }
    }

    /**
     * Returns true if the rule has never fired or the cooldown window has elapsed.
     */
    private boolean shouldNotify(AlertRule rule) {
        if (rule.getLastTriggeredAt() == null) return true;
        long minutesSince = Duration.between(rule.getLastTriggeredAt(), LocalDateTime.now()).toMinutes();
        return minutesSince >= rule.getCooldownMinutes();
    }
}