package com.example.app.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic cartItemEventsTopic() {
    return TopicBuilder.name("cart-item-events").partitions(1).replicas(1).build();
  }
}
