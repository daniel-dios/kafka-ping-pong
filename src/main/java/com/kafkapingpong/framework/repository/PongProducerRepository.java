package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.repository.dto.MessageOut;
import com.kafkapingpong.framework.repository.dto.PayloadError;
import com.kafkapingpong.framework.repository.dto.PayloadSuccess;
import com.kafkapingpong.framework.repository.exception.MessageNotSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.UUID;

public class PongProducerRepository implements PongRepository {
  public static final String PARTITION_KEY = "partitionKey";

  private final Logger logger = LoggerFactory.getLogger(PongProducerRepository.class);

  private final Duration timeout;
  private final MessageChannel pongSuccessChannel;
  private final MessageChannel pongErrorChannel;
  private final MessageChannel dlqChannel;

  public PongProducerRepository(
      Duration timeout,
      MessageChannel pongSuccessChannel,
      MessageChannel pongErrorChannel,
      MessageChannel dlqChannel) {
    this.timeout = timeout;
    this.pongSuccessChannel = pongSuccessChannel;
    this.pongErrorChannel = pongErrorChannel;
    this.dlqChannel = dlqChannel;
  }

  @Override
  public void pong(Message message, Duration duration) {
    send(pongSuccessChannel, message.getTransactionId(), mapToPongSuccess(message, duration));
  }

  @Override
  public void pongForError(Message message) {
    send(pongErrorChannel, message.getTransactionId(), mapToPongError(message));
  }

  @Override
  public void dlq(Message message) {
    send(dlqChannel, message.getTransactionId(), mapToPongError(message));
  }

  private void send(MessageChannel messageChannel, UUID transactionId, MessageOut message) {
    final var outMessage = MessageBuilder
        .withPayload(message)
        .setHeader(PARTITION_KEY, transactionId.toString())
        .build();

    if (!messageChannel.send(outMessage, timeout.toMillis())) {
      logger.info("{} error when sending to {}", message.getTransactionId(), messageChannel);
      throw new MessageNotSendException();
    }

    logger.info("{} was successfully sent to {}", message.getTransactionId(), messageChannel);
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
