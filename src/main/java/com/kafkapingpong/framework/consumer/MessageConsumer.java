package com.kafkapingpong.framework.consumer;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.consumer.dto.MessageIn;
import com.kafkapingpong.service.Processor;

import java.util.function.Consumer;

public class MessageConsumer implements Consumer<MessageIn> {

  private final Processor processor;

  public MessageConsumer(Processor processor) {
    this.processor = processor;
  }

  @Override
  public void accept(MessageIn messageIn) {
    processor.process(new Message(messageIn.id, new Payload(messageIn.payload.message, messageIn.payload.forceError)));
  }
}
