package com.kafkapingpong.event;

import java.time.Duration;
import java.util.UUID;

public class PongMessage {
  private final UUID transactionType;
  private final String pong;
  private final Duration ofMillis;

  public PongMessage(UUID transactionType, String pong, Duration ofMillis) {
    this.transactionType = transactionType;
    this.pong = pong;
    this.ofMillis = ofMillis;
  }

  public UUID getTransactionType() {
    return transactionType;
  }

  public String getPong() {
    return pong;
  }

  public Duration getOfMillis() {
    return ofMillis;
  }
}
