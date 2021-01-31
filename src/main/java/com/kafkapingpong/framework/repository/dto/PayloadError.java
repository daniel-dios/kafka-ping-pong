package com.kafkapingpong.framework.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadError extends PayloadOut {
  @JsonProperty("force_error")
  private final boolean forceError;

  public PayloadError(String message, boolean forceError) {
    super(message);
    this.forceError = forceError;
  }

  public boolean isForceError() {
    return forceError;
  }
}
