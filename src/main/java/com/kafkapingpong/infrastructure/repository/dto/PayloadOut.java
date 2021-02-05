package com.kafkapingpong.infrastructure.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class PayloadOut {
  @JsonProperty("message")
  private String message;

  protected PayloadOut() {
  }

  protected PayloadOut(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
