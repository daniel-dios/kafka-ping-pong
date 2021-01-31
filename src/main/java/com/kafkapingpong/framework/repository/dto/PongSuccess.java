package com.kafkapingpong.framework.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PongSuccess {
  @JsonProperty("transaction-id")
  private final String transactionId;

  @JsonProperty("payload")
  private final Payload payload;

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
