package com.kafkapingpong.infrastructure.repository;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.producer.ErrorProducer;
import com.kafkapingpong.infrastructure.producer.SuccessProducer;

import java.time.Duration;

public class PongProducerRepository implements PongRepository {
  private final SuccessProducer pongSuccessProducer;
  private final ErrorProducer pongErrorProducer;
  private final ErrorProducer dlqProducer;

  public PongProducerRepository(
      SuccessProducer pongSuccessProducer,
      ErrorProducer pongErrorProducer,
      ErrorProducer dlqProducer) {
    this.pongSuccessProducer = pongSuccessProducer;
    this.pongErrorProducer = pongErrorProducer;
    this.dlqProducer = dlqProducer;
  }

  @Override
  public void pong(Message message, Duration duration) {
    pongSuccessProducer.sendSuccess(message, duration);
  }

  @Override
  public void pongForError(Message message) {
    pongErrorProducer.sendError(message);
  }

  @Override
  public void dlq(Message message) {
    dlqProducer.sendError(message);
  }
}
