package com.kafkapingpong.event;

import java.time.Duration;
import java.util.UUID;

public class PongMessage {
  private final UUID transactionId;
  private final String pong;
  private final Duration ofMillis;

  public PongMessage(UUID transactionId, String pong, Duration ofMillis) {
    this.transactionId = transactionId;
    this.pong = pong;
    this.ofMillis = ofMillis;
  }

  public UUID getTransactionId() {
    return transactionId;
  }

  public String getPong() {
    return pong;
  }

  public Duration getOfMillis() {
    return ofMillis;
  }
}
