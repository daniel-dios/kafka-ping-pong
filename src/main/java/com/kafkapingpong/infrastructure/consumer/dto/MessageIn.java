package com.kafkapingpong.infrastructure.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageIn {

  @JsonProperty("transaction-id")
  public UUID id;

  public Payload payload;
}
