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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;

    if (error != message.error) {
      return false;
    }
    return transactionId.equals(message.transactionId);
  }

  @Override
  public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (error ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Message{" +
        "transactionId=" + transactionId +
        ", error=" + error +
        '}';
  }
}
