package com.market.monitor.notification;

import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.model.AlertRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Sends alert notifications to a DingTalk custom robot webhook.
 * Runs on the dedicated notificationExecutor thread pool (@Async).
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class DingTalkNotificationService {

    private final RestClient restClient;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async("notificationExecutor")
    public void send(AlertRule rule, MarketPriceDTO price) {
        try {
            String content = buildContent(rule, price);
            Map<String, Object> body = Map.of(
                    "msgtype", "text",
                    "text", Map.of("content", content)
            );

            restClient.post()
                    .uri(rule.getNotifyTarget())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("DingTalk notification sent for rule[{}]", rule.getId());

        } catch (Exception e) {
            log.error("DingTalk notification FAILED for rule[{}]: {}", rule.getId(), e.getMessage());
        }
    }

    private String buildContent(AlertRule rule, MarketPriceDTO price) {
        String condition = rule.getConditionType() == ConditionType.ABOVE ? "超过 (ABOVE)" : "低于 (BELOW)";
        return String.format(
                "[Market Alert] %s\n" +
                "资产: %s (%s)\n" +
                "触发条件: 价格%s $%s\n" +
                "当前价格: $%s\n" +
                "时间: %s",
                rule.getName(),
                rule.getSymbol().toUpperCase(), rule.getAssetType(),
                condition, rule.getThresholdPrice().toPlainString(),
                price.getPrice().toPlainString(),
                price.getFetchedAt().format(FMT)
        );
    }
}