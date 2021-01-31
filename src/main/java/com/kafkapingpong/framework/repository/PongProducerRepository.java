package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.repository.dto.Payload;
import com.kafkapingpong.framework.repository.dto.PongSuccess;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;

public class PongProducerRepository implements PongRepository {
  public static final String PARTITION_KEY = "partitionKey";

  private final MessageChannel pongSuccessChannel;

  public PongProducerRepository(MessageChannel pongSuccessChannel) {
    this.pongSuccessChannel = pongSuccessChannel;
  }

  @Override
  public void pong(Message message, Duration duration) {
    final var messageToSend = MessageBuilder
        .withPayload(mapToPongSuccess(message, duration))
        .setHeader(PARTITION_KEY, message.getTransactionId().toString())
        .build();

    pongSuccessChannel.send(messageToSend);
  }

  @Override
  public void pongForError(Message message) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void dlq(Message message) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  private PongSuccess mapToPongSuccess(Message message, Duration duration) {
    return new PongSuccess(
        message.getTransactionId().toString(),
        new Payload("pong", duration.toMillis()));
  }
}
