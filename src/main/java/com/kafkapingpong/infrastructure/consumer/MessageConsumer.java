package com.kafkapingpong.infrastructure.consumer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.infrastructure.consumer.dto.MessageIn;
import com.kafkapingpong.usecase.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MessageConsumer implements Consumer<MessageIn> {
  private final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
  private final Processor processor;

  public MessageConsumer(Processor processor) {
    this.processor = processor;
  }

  @Override
  public void accept(MessageIn messageIn) {
    logger.info("received {}", messageIn.id);
    processor.process(new Message(messageIn.id, new Payload(messageIn.payload.message, messageIn.payload.forceError)));
    logger.info("processed {}", messageIn.id);
  }
}
