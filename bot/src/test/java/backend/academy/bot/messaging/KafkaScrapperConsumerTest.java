package backend.academy.bot.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.messaging.kafka.KafkaScrapperConsumer;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        properties = {
            "messaging.message-transport=Kafka",
            "spring.kafka.consumer.group-id=test-group",
            "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@Testcontainers
public class KafkaScrapperConsumerTest {
    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.5.3").asCompatibleSubstituteFor("apache/kafka"))
            .waitingFor(Wait.forLogMessage(".*Transitioning from RECOVERY to RUNNING.*", 1)
                    .withStartupTimeout(Duration.ofSeconds(120)));

    @Autowired
    private KafkaTemplate<String, IncomingUpdate> kafkaTemplate;

    @Value("${spring.kafka.topics.link-update.name}")
    private String linkUpdateTopic;

    @Value("${spring.kafka.topics.link-update-dlq.name}")
    private String dlqTopic;

    @SpyBean
    private KafkaScrapperConsumer kafkaScrapperConsumer;

    @SpyBean
    private ApplicationEventPublisher eventPublisher;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.producer.transaction-id-prefix", () -> null);
    }

    @Test
    void whenValidMessageSent_thenConsumerProcessesAndPublishesEvent() throws Exception {
        IncomingUpdate validUpdate = new IncomingUpdate(
                1L, "http://example.com/valid", "Valid link update", (java.util.List<Long>) Set.of(1L));

        kafkaTemplate.send(linkUpdateTopic, validUpdate).get(10, TimeUnit.SECONDS);

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    verify(eventPublisher, timeout(5000)).publishEvent(any());
                });

        verify(kafkaScrapperConsumer, timeout(5000)).listen(validUpdate);
    }

    @Test
    void whenInvalidMessageSent_thenConsumerThrowsExceptionAndMessageGoesToDLQ() throws Exception {

        IncomingUpdate invalidUpdate =
                new IncomingUpdate(2L, null, "Invalid link update", (java.util.List<Long>) Set.of(2L));

        kafkaTemplate.send(linkUpdateTopic, invalidUpdate).get(10, TimeUnit.SECONDS);

        try (org.apache.kafka.clients.consumer.KafkaConsumer<String, IncomingUpdate> dlqConsumer =
                new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                        KafkaTestUtils.consumerProps("dlq-test-group", "true", KAFKA.getBootstrapServers()),
                        new org.apache.kafka.common.serialization.StringDeserializer(),
                        new org.springframework.kafka.support.serializer.JsonDeserializer<>(IncomingUpdate.class))) {

            dlqConsumer.subscribe(List.of(dlqTopic));

            org.apache.kafka.clients.consumer.ConsumerRecords<String, IncomingUpdate> records =
                    KafkaTestUtils.getRecords(dlqConsumer, Duration.ofSeconds(15), 1);

            assertThat(records).hasSize(1);

            org.apache.kafka.clients.consumer.ConsumerRecord<String, IncomingUpdate> dlqRecord =
                    records.iterator().next();

            assertThat(dlqRecord.value()).isEqualTo(invalidUpdate);

            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
