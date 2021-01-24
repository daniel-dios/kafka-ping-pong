package com.kafkapingpong.framework.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {

  @JsonProperty("message")
  public String message;

  @JsonProperty("force_error")
  public boolean forceError;

  String getMessage() {
    return message;
  }

  boolean isForceError() {
    return forceError;
  }
}
