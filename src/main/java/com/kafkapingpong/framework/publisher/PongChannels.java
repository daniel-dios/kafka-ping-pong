package com.kafkapingpong.framework.publisher;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PongChannels {
  String PONG_OUTPUT_CHANNEL = "pong";

  @Output(PONG_OUTPUT_CHANNEL)
  MessageChannel getPongChannel();
}