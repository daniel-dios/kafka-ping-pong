package com.kafkapingpong.framework.helper.kafka;

import java.util.List;

public class KafkaConstants {
  private KafkaConstants() {
    // Constants class
  }

  public static final String KAFKA_HOST = "localhost";
  public static final int KAFKA_PORT = 9094;
  public static final String PING_TOPIC = "ping";
  public static final String PONG_TOPIC = "pong";
  public static final String PONG_ERROR = "pong-error";
  public static final String DLQ = "dlq";
  public static final List<String> TOPICS = List.of(PING_TOPIC, PONG_TOPIC, PONG_ERROR, DLQ);
}
