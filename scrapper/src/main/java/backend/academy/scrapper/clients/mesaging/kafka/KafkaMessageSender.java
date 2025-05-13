package backend.academy.scrapper.clients.mesaging.kafka;

import backend.academy.scrapper.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageSender {
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final String topicName;

    public KafkaMessageSender(
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            @Value("${spring.kafka.topics.link-update.name}") String topicName) {

        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @RateLimiter(name = "kafkaRateLimiter")
    @Retry(name = "kafkaRetry")
    @CircuitBreaker(name = "kafkaCircuitBreaker", fallbackMethod = "fallbackSend")
    public void send(LinkUpdate message) {
        kafkaTemplate.send(topicName, message.id().toString(), message);
    }

    public void fallbackSend(LinkUpdate message, Throwable t) {
        log.error("Failed to send message to Kafka topic {}. Error: {}", topicName, t.getMessage());
    }
}
