package backend.academy.scrapper;

import backend.academy.scrapper.config.DomainsConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableRetry
@EnableConfigurationProperties({ScrapperConfig.class, DomainsConfig.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
