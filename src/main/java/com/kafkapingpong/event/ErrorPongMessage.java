package com.kafkapingpong.event;

import java.util.UUID;

public class ErrorPongMessage {
  private final UUID transactionID;
  private final String pong;
  private final boolean error;

  public ErrorPongMessage(UUID transactionID, String pong, boolean error) {
    this.transactionID = transactionID;
    this.pong = pong;
    this.error = error;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ErrorPongMessage that = (ErrorPongMessage) o;

    if (error != that.error) {
      return false;
    }
    if (!transactionID.equals(that.transactionID)) {
      return false;
    }
    return pong.equals(that.pong);
  }

  @Override
  public int hashCode() {
    int result = transactionID.hashCode();
    result = 31 * result + pong.hashCode();
    result = 31 * result + (error ? 1 : 0);
    return result;
  }
}
