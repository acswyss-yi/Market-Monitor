package com.market.monitor.notification;

import com.market.monitor.dto.MarketPriceDTO;
import com.market.monitor.enums.ConditionType;
import com.market.monitor.model.AlertRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Email notification via Spring JavaMailSender.
 *
 * <p>This bean is only created when spring.mail.host is configured —
 * app starts cleanly without mail settings; NotificationRouter logs a warning instead.
 *
 * <p>Runs async so slow SMTP handshakes don't block the scheduler thread.
 */
@Service
@Log4j2
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class MailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async("notificationExecutor")
    public void send(AlertRule rule, MarketPriceDTO price) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(rule.getNotifyTarget());
            msg.setSubject(buildSubject(rule));
            msg.setText(buildBody(rule, price));
            mailSender.send(msg);
            log.info("Email sent to {} for rule[{}]", rule.getNotifyTarget(), rule.getId());
        } catch (Exception e) {
            log.error("Email FAILED for rule[{}]: {}", rule.getId(), e.getMessage());
        }
    }

    private String buildSubject(AlertRule rule) {
        String direction = rule.getConditionType() == ConditionType.ABOVE ? "exceeded" : "fell below";
        return String.format("[Market Alert] %s %s $%s",
                rule.getSymbol().toUpperCase(), direction, rule.getThresholdPrice().toPlainString());
    }

    private String buildBody(AlertRule rule, MarketPriceDTO price) {
        String condition = rule.getConditionType() == ConditionType.ABOVE ? "above" : "below";
        return String.format(
                "Your market alert has been triggered!\n\n" +
                "Rule    : %s\n" +
                "Asset   : %s (%s)\n" +
                "Condition: price %s $%s\n" +
                "Current : $%s\n" +
                "Time    : %s\n\n" +
                "-- Market Monitor",
                rule.getName(),
                rule.getSymbol().toUpperCase(), rule.getAssetType(),
                condition, rule.getThresholdPrice().toPlainString(),
                price.getPrice().toPlainString(),
                price.getFetchedAt().format(FMT)
        );
    }
}