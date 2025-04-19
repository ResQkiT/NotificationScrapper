package backend.academy.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "domains")
public record DomainsConfig(String scrapper) {}
