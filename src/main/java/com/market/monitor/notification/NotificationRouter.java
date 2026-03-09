package com.market.monitor.notification;

import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.model.AlertRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Routes a triggered alert to the correct notification channel.
 * MailNotificationService is injected as Optional because it only exists
 * when spring.mail.host is configured — avoids startup failure.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class NotificationRouter {

    private final Optional<MailNotificationService> mailService;
    private final DingTalkNotificationService dingTalkService;

    public void route(AlertRule rule, MarketPriceDTO price) {
        switch (rule.getNotifyChannel()) {
            case EMAIL -> mailService.ifPresentOrElse(
                    svc -> svc.send(rule, price),
                    () -> log.warn("EMAIL alert for rule[{}] skipped: spring.mail.host not configured", rule.getId())
            );
            case DINGTALK -> dingTalkService.send(rule, price);
        }
    }
}