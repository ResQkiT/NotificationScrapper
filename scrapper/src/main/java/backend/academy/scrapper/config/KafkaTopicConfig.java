package backend.academy.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topics.link-update.name}")
    private String linkUpdatesTopicName;

    @Value("${spring.kafka.topics.link-update.partitions}")
    private int linkUpdatesPartitions;

    @Value("${spring.kafka.topics.link-update.replicas}")
    private int linkUpdatesReplicas;

    @Bean
    public NewTopic linkUpdateTopic() {
        return TopicBuilder.name(linkUpdatesTopicName)
                .partitions(linkUpdatesPartitions)
                .replicas(linkUpdatesReplicas)
                .build();
    }
}
