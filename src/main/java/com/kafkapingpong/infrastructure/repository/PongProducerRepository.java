package com.kafkapingpong.infrastructure.repository;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.producer.MessageOutProducer;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadError;
import com.kafkapingpong.infrastructure.repository.dto.PayloadSuccess;

import java.time.Duration;

public class PongProducerRepository implements PongRepository {
  private final MessageOutProducer pongSuccessProducer;
  private final MessageOutProducer pongErrorProducer;
  private final MessageOutProducer dlqProducer;

  public PongProducerRepository(
      MessageOutProducer pongSuccessProducer,
      MessageOutProducer pongErrorProducer,
      MessageOutProducer dlqProducer) {
    this.pongSuccessProducer = pongSuccessProducer;
    this.pongErrorProducer = pongErrorProducer;
    this.dlqProducer = dlqProducer;
  }

  @Override
  public void pong(Message message, Duration duration) {
    pongSuccessProducer.send(mapToPongSuccess(message, duration));
  }

  @Override
  public void pongForError(Message message) {
    pongErrorProducer.send(mapToPongError(message));
  }

  @Override
  public void dlq(Message message) {
    dlqProducer.send(mapToPongError(message));
  }

  private MessageOut mapToPongSuccess(Message message, Duration duration) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadSuccess("pong", duration.toMillis()));
  }

  private MessageOut mapToPongError(Message message) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadError("ping", message.isError()));
  }
}
