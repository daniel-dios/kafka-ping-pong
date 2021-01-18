package com.kafkapingpong.service.dto;

import java.util.UUID;

public class ProcessRequest {
  private final UUID transactionType;
  private final boolean error;

  public ProcessRequest(UUID transactionType, boolean error) {
    this.transactionType = transactionType;
    this.error = error;
  }

  public UUID getTransactionType() {
    return transactionType;
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
    return transactionType.equals(that.transactionType);
  }

  @Override
  public int hashCode() {
    int result = transactionType.hashCode();
    result = 31 * result + (error ? 1 : 0);
    return result;
  }
}
