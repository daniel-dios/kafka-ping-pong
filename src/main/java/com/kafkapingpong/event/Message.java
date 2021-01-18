package com.kafkapingpong.event;

import java.util.UUID;

public class Message {
  private final UUID transactionType;
  private final boolean error;

  public Message(UUID transactionType, boolean error) {
    this.transactionType = transactionType;
    this.error = error;
  }

  public UUID getTransactionType() {
    return transactionType;
  }

  public boolean isError() {
    return error;
  }
}
