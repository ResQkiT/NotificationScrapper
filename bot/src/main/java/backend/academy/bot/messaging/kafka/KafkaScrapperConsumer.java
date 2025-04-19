package backend.academy.bot.messaging.kafka;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.messaging.ScrapperController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "messaging.message-transport", havingValue = "Kafka")
public class KafkaScrapperConsumer extends ScrapperController {

    @Autowired
    public KafkaScrapperConsumer(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @KafkaListener(
            topics = "#{'${spring.kafka.topics.link-update.name}'}",
            groupId = "${spring.kafka.consumer.group-id}",
            errorHandler = "kafkaErrorHandler")
    public void listen(@Payload IncomingUpdate update) {
        log.info("Received new kafka link update");
        if (update == null || update.url() == null || update.url().isEmpty() || update.tgChatIds() == null) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        sendNotification(update);
    }
}
