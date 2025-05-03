package backend.academy.scrapper.clients.mesaging.kafka;

import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
public class KafkaBotProducer implements IClient {
    private final KafkaMessageSender kafkaMessageSender;

    @Autowired
    public KafkaBotProducer(KafkaMessageSender kafkaMessageSender) {
        this.kafkaMessageSender = kafkaMessageSender;
    }

    @Override
    public boolean send(LinkUpdate message) {
        try {
            kafkaMessageSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
