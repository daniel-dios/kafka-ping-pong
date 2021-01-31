package com.kafkapingpong.framework.configuration;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface PongChannels {
  String PONG_OUTPUT = "pong";
  String PING_INPUT = "ping";

  @Input(PING_INPUT)
  SubscribableChannel getInput();

  @Output(PONG_OUTPUT)
  MessageChannel getPongChannel();
}