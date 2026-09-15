package com.example.app.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CartEventListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(CartEventListener.class);

  @KafkaListener(topics = "cart-item-events")
  public void consume(CartItemEvent event) {
    LOGGER.info("Cart event received: {}", event);
  }
}
