package com.market.monitor.controller;

import com.market.monitor.dto.AlertRuleRequest;
import com.market.monitor.dto.AlertRuleResponse;
import com.market.monitor.service.AlertRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert-rules")
@RequiredArgsConstructor
@Tag(name = "Alert Rules", description = "CRUD for price alert rules")
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @GetMapping
    @Operation(summary = "List all alert rules")
    public List<AlertRuleResponse> listAll() {
        return alertRuleService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rule by ID")
    public AlertRuleResponse getById(@PathVariable Long id) {
        return alertRuleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new alert rule")
    public AlertRuleResponse create(@RequestBody @Valid AlertRuleRequest request) {
        return alertRuleService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing alert rule")
    public AlertRuleResponse update(@PathVariable Long id,
                                    @RequestBody @Valid AlertRuleRequest request) {
        return alertRuleService.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle enable/disable; resets cooldown so rule can fire immediately on re-enable")
    public AlertRuleResponse toggle(@PathVariable Long id) {
        return alertRuleService.toggle(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an alert rule")
    public void delete(@PathVariable Long id) {
        alertRuleService.delete(id);
    }
}