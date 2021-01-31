package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.repository.dto.MessageOut;
import com.kafkapingpong.framework.repository.dto.PayloadError;
import com.kafkapingpong.framework.repository.dto.PayloadSuccess;
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

  private MessageOut mapToPongSuccess(Message message, Duration duration) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadSuccess("pong", duration.toMillis()));
  }

  private MessageOut mapToPongError(Message message) {
    return new MessageOut(message.getTransactionId().toString(),
        new PayloadError("ping", message.isError()));
  }
}
