package com.kafkapingpong.service.dto;

import java.util.UUID;

public class ProcessRequest {
  private final UUID transactionId;
  private final boolean error;

  public ProcessRequest(UUID transactionId, boolean error) {
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

    ProcessRequest that = (ProcessRequest) o;

    if (error != that.error) {
      return false;
    }
    return transactionId.equals(that.transactionId);
  }

  @Override
  public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (error ? 1 : 0);
    return result;
  }
}
