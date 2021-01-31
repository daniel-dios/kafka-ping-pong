package com.kafkapingpong.framework.repository.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {

  @JsonProperty("message")
  private String message;

  @JsonProperty("force_error")
  private boolean forceError;

  public Payload() {
  }

  public Payload(String message, boolean forceError) {
    this.message = message;
    this.forceError = forceError;
  }

  public String getMessage() {
    return message;
  }

  public boolean isForceError() {
    return forceError;
  }
}
