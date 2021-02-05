package com.kafkapingpong.infrastructure.consumer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.infrastructure.consumer.dto.MessageIn;
import com.kafkapingpong.usecase.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;

import static com.kafkapingpong.infrastructure.configuration.PongChannels.PING_INPUT;

public class MessageConsumer {
  private final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
  private final Processor processor;

  public MessageConsumer(Processor processor) {
    this.processor = processor;
  }

  @StreamListener(PING_INPUT)
  public void consume(MessageIn messageIn) {
    logger.info("received {}", messageIn.id);
    processor.process(new Message(messageIn.id, new Payload(messageIn.payload.message, messageIn.payload.forceError)));
    logger.info("processed {}", messageIn.id);
  }
}
