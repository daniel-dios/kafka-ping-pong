package com.kafkapingpong.event;

import java.util.UUID;

public class Message {
  private final UUID transactionId;
  private final boolean error;

  public Message(UUID transactionId, boolean error) {
    this.transactionId = transactionId;
    this.error = error;
  }

  public UUID getTransactionId() {
    return transactionId;
  }

  public boolean isError() {
    return error;
  }
}
