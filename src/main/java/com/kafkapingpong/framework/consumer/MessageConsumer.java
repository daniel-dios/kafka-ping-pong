package com.kafkapingpong.framework.consumer;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.consumer.dto.MessageIn;
import com.kafkapingpong.service.Processor;
import org.springframework.cloud.stream.annotation.StreamListener;

import static com.kafkapingpong.framework.configuration.PongChannels.PING_INPUT;

public class MessageConsumer {

  private final Processor processor;

  public MessageConsumer(Processor processor) {
    this.processor = processor;
  }

  @StreamListener(PING_INPUT)
  public void consume(MessageIn messageIn) {
    processor.process(new Message(messageIn.id, new Payload(messageIn.payload.message, messageIn.payload.forceError)));
  }
}
