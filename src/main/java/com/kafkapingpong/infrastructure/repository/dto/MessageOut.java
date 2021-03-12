package com.kafkapingpong.infrastructure.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageOut {
  @JsonProperty("transaction-id")
  private final String transactionId;

  @JsonProperty("payload")
  private final PayloadOut payload;

  public MessageOut(String transactionId, PayloadOut payload) {
    this.transactionId = transactionId;
    this.payload = payload;
  }

  public PayloadOut getPayload() {
    return payload;
  }
}
