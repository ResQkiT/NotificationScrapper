package backend.academy.scrapper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "domains")
public record DomainsConfig(String github, String stackoverflow, String bot, String telegramBotUrl) {
}
