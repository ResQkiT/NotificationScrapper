package backend.academy.bot.messaging.kafka;

import backend.academy.bot.dto.IncomingUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaErrorHandler")
public class KafkaLinkUpdateErrorHandler implements KafkaListenerErrorHandler {

    private final KafkaTemplate<String, IncomingUpdate> kafkaTemplate;
    private final String dlqTopic;

    public KafkaLinkUpdateErrorHandler(
            KafkaTemplate<String, IncomingUpdate> kafkaTemplate,
            @Value("${spring.kafka.topics.link-update-dlq.name}") String dlqTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.dlqTopic = dlqTopic;
    }

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error(
                "Error processing message from topic: {}, offset: {}. Sending to DLQ. Error: {}",
                message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC),
                message.getHeaders().get(KafkaHeaders.OFFSET),
                exception.getMessage());

        Object payload = message.getPayload();

        if (payload instanceof IncomingUpdate) {
            kafkaTemplate.send(dlqTopic, (IncomingUpdate) payload);
        } else {
            log.error(
                    "Failed to send message to DLQ: Unexpected payload type {}",
                    payload != null ? payload.getClass().getName() : "null");
        }

        return null;
    }
}
