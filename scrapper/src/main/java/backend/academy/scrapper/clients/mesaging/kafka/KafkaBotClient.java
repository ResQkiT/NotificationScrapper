package backend.academy.scrapper.clients.mesaging.kafka;

import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "messaging.message-transport", havingValue = "Kafka")
public class KafkaBotClient implements IClient {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final String topicName;

    public KafkaBotClient(
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            @Value("${spring.kafka.topics.link-update.name}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void send(LinkUpdate message) {
        kafkaTemplate.send(topicName, message.id().toString(), message);
    }
}
