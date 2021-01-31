package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.repository.dto.error.PongError;
import com.kafkapingpong.framework.repository.dto.success.Payload;
import com.kafkapingpong.framework.repository.dto.success.PongSuccess;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;

public class PongProducerRepository implements PongRepository {
  public static final String PARTITION_KEY = "partitionKey";

  private final MessageChannel pongSuccessChannel;
  private final MessageChannel pongErrorChannel;
  private final MessageChannel dlqChannel;

  public PongProducerRepository(
      MessageChannel pongSuccessChannel,
      MessageChannel pongErrorChannel,
      MessageChannel dlqChannel) {
    this.pongSuccessChannel = pongSuccessChannel;
    this.pongErrorChannel = pongErrorChannel;
    this.dlqChannel = dlqChannel;
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
    final var messageToSend = MessageBuilder
        .withPayload(mapToPongError(message))
        .setHeader(PARTITION_KEY, message.getTransactionId().toString())
        .build();

    pongErrorChannel.send(messageToSend);
  }

  @Override
  public void dlq(Message message) {
    final var messageToSend = MessageBuilder
        .withPayload(mapToPongError(message))
        .setHeader(PARTITION_KEY, message.getTransactionId().toString())
        .build();

    dlqChannel.send(messageToSend);
  }

  private PongSuccess mapToPongSuccess(Message message, Duration duration) {
    return new PongSuccess(
        message.getTransactionId().toString(),
        new Payload("pong", duration.toMillis()));
  }

  private PongError mapToPongError(Message message) {
    return new PongError(message.getTransactionId().toString(),
        new com.kafkapingpong.framework.repository.dto.error.Payload("ping", message.isError()));
  }
}
