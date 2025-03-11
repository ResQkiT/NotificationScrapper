package backend.academy.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "domains")
public record DomainsConfig(String scrapper) {}
