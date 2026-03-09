package com.market.monitor.service;

import com.market.monitor.dto.AlertRuleRequest;
import com.market.monitor.dto.AlertRuleResponse;
import com.market.monitor.exception.ResourceNotFoundException;
import com.market.monitor.mapper.AlertRuleMapper;
import com.market.monitor.model.AlertRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;

    public List<AlertRuleResponse> findAll() {
        return alertRuleMapper.findAll().stream()
                .map(AlertRuleResponse::from)
                .toList();
    }

    public AlertRuleResponse findById(Long id) {
        return alertRuleMapper.findById(id)
                .map(AlertRuleResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", id));
    }

    public AlertRuleResponse create(AlertRuleRequest req) {
        AlertRule rule = AlertRule.builder()
                .name(req.getName())
                .assetType(req.getAssetType())
                .symbol(req.getSymbol().toLowerCase())
                .conditionType(req.getConditionType())
                .thresholdPrice(req.getThresholdPrice())
                .notifyChannel(req.getNotifyChannel())
                .notifyTarget(req.getNotifyTarget())
                .enabled(true)
                .cooldownMinutes(req.getCooldownMinutes() != null ? req.getCooldownMinutes() : 60)
                .build();
        alertRuleMapper.insert(rule);
        return AlertRuleResponse.from(rule);
    }

    public AlertRuleResponse update(Long id, AlertRuleRequest req) {
        AlertRule rule = alertRuleMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", id));
        rule.setName(req.getName());
        rule.setAssetType(req.getAssetType());
        rule.setSymbol(req.getSymbol().toLowerCase());
        rule.setConditionType(req.getConditionType());
        rule.setThresholdPrice(req.getThresholdPrice());
        rule.setNotifyChannel(req.getNotifyChannel());
        rule.setNotifyTarget(req.getNotifyTarget());
        if (req.getCooldownMinutes() != null) {
            rule.setCooldownMinutes(req.getCooldownMinutes());
        }
        alertRuleMapper.update(rule);
        return AlertRuleResponse.from(rule);
    }

    public AlertRuleResponse toggle(Long id) {
        AlertRule rule = alertRuleMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", id));
        boolean next = !rule.isEnabled();
        alertRuleMapper.toggleEnabled(id, next);
        rule.setEnabled(next);
        rule.setLastTriggeredAt(null);
        return AlertRuleResponse.from(rule);
    }

    public void delete(Long id) {
        alertRuleMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", id));
        alertRuleMapper.deleteById(id);
    }
}