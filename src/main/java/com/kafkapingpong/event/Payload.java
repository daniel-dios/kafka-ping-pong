package com.kafkapingpong.event;

public class Payload {
  private final String message;
  private final boolean forceError;

  public Payload(String message, boolean forceError) {
    this.message = message;
    this.forceError = forceError;
  }

  public boolean isForceError() {
    return forceError;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Payload payload = (Payload) o;

    if (forceError != payload.forceError) {
      return false;
    }
    return message.equals(payload.message);
  }

  @Override
  public int hashCode() {
    int result = message.hashCode();
    result = 31 * result + (forceError ? 1 : 0);
    return result;
  }
}
