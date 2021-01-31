package com.kafkapingpong.framework.repository.dto.success;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PongSuccess {
  @JsonProperty("transaction-id")
  private String transactionId;

  @JsonProperty("payload")
  private Payload payload;

  public PongSuccess() {
  }

  public PongSuccess(String transactionId, Payload payload) {
    this.transactionId = transactionId;
    this.payload = payload;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public Payload getPayload() {
    return payload;
  }
}
