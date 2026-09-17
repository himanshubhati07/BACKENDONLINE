package com.example.app.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class GymEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${gym.kafka.topic}")
  private String topic;

  public GymEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(String eventType, String payload) {
    String message = "{\"eventType\":\"" + eventType + "\",\"payload\":" + payload + "}";
    kafkaTemplate.send(topic, eventType, message);
  }
}
