package com.market.monitor.mapper;

import com.market.monitor.model.AlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AlertRuleMapper {

    List<AlertRule> findAll();

    Optional<AlertRule> findById(@Param("id") Long id);

    /** Returns only enabled rules — used by schedulers and evaluation engine. */
    List<AlertRule> findAllEnabled();

    int insert(AlertRule alertRule);

    int update(AlertRule alertRule);

    int deleteById(@Param("id") Long id);

    /** Stamp the last notification time without touching other fields. */
    int updateLastTriggeredAt(@Param("id") Long id, @Param("lastTriggeredAt") LocalDateTime lastTriggeredAt);

    /** Enable/disable and reset lastTriggeredAt so the rule can fire again immediately. */
    int toggleEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);
}