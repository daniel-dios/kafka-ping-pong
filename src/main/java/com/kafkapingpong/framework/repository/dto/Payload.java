package com.kafkapingpong.framework.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {
  @JsonProperty("message")
  private final String message;

  @JsonProperty("processing_time")
  private final long processingTime;

  public Payload(String message, long processingTime) {
    this.message = message;
    this.processingTime = processingTime;
  }

  public String getMessage() {
    return message;
  }

  public long getProcessingTime() {
    return processingTime;
  }
}
