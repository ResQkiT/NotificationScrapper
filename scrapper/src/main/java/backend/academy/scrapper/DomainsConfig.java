package backend.academy.scrapper;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "domains")
public record DomainsConfig(String github, String stackoverflow, String bot, String telegramBotUrl) {

    @PostConstruct
    public void print() {
        System.out.println("Github token: " + github());
        System.out.println("Stackoverflow token: " + stackoverflow());
        System.out.println("Bot: " + bot());
        System.out.println("Telegram Bot URL: " + telegramBotUrl());
    }
}
