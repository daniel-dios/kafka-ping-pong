package com.kafkapingpong.framework.configuration;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface PongChannels {
  String PING_INPUT = "ping";

  @Input(PING_INPUT)
  SubscribableChannel getInput();

  String PONG_OUTPUT = "pong";

  @Output(PONG_OUTPUT)
  MessageChannel getPongChannel();

  String PONG_ERROR_OUTPUT = "pong-error";

  @Output(PONG_ERROR_OUTPUT)
  MessageChannel getPongErrorChannel();
}