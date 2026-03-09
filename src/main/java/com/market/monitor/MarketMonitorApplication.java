package com.market.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.market.monitor.mapper")
public class MarketMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketMonitorApplication.class, args);
    }
}