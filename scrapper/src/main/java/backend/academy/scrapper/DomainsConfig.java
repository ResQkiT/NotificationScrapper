package backend.academy.scrapper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties
public class DomainsConfig {
    private String github = "https://api.github.com";
    private String stackoverflow = "https://api.stackexchange.com/2.3";
    private String bot = "http://localhost:8090";
    private String telegramBotUrl = "http://localhost:8080";
}
