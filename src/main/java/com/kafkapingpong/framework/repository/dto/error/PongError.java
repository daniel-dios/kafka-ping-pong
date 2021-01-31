package com.kafkapingpong.framework.repository.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PongError {

  @JsonProperty("transaction-id")
  private String id;

  private Payload payload;

  public PongError() {
  }

  public PongError(String id, Payload payload) {
    this.id = id;
    this.payload = payload;
  }

  public String getId() {
    return id;
  }

  public Payload getPayload() {
    return payload;
  }
}
