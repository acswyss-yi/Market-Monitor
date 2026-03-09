package com.market.monitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI marketMonitorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Market Monitor API")
                        .description("Financial market price monitoring and alert system. " +
                                "Supports crypto (CoinGecko) and stocks (Alpha Vantage).")
                        .version("1.0.0"));
    }
}