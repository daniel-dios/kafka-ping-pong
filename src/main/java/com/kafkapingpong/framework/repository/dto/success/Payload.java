package com.kafkapingpong.framework.repository.dto.success;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {
  @JsonProperty("message")
  private String message;

  @JsonProperty("processing_time")
  private long processingTime;

  public Payload() {
  }

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
