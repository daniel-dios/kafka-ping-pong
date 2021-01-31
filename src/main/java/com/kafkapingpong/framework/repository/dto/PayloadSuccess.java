package com.kafkapingpong.framework.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadSuccess extends PayloadOut {

  @JsonProperty("processing_time")
  private final long processingTime;

  public PayloadSuccess(String message, long processingTime) {
    super(message);
    this.processingTime = processingTime;
  }

  public long getProcessingTime() {
    return processingTime;
  }
}
