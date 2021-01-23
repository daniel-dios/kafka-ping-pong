package com.kafkapingpong.event;

import java.util.UUID;

public class Message {
  private final UUID transactionId;
  private final Payload payload;

  public Message(UUID transactionId, Payload payload) {
    this.transactionId = transactionId;
    this.payload = payload;
  }

  public UUID getTransactionId() {
    return transactionId;
  }

  public boolean isError() {
    return payload.isForceError();
  }

  public String getMessage() {
    return payload.getMessage();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;

    if (!transactionId.equals(message.transactionId)) {
      return false;
    }
    return payload.equals(message.payload);
  }

  @Override
  public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + payload.hashCode();
    return result;
  }
}
