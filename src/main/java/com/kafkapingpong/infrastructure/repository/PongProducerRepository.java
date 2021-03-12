package com.kafkapingpong.infrastructure.repository;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.producer.DLQProducer;
import com.kafkapingpong.infrastructure.producer.PongErrorProducer;
import com.kafkapingpong.infrastructure.producer.PongSuccessProducer;

import java.time.Duration;

public class PongProducerRepository implements PongRepository {
  private final PongSuccessProducer pongSuccessProducer;
  private final PongErrorProducer pongErrorProducer;
  private final DLQProducer dlqProducer;

  public PongProducerRepository(
      PongSuccessProducer pongSuccessProducer,
      PongErrorProducer pongErrorProducer,
      DLQProducer dlqProducer) {
    this.pongSuccessProducer = pongSuccessProducer;
    this.pongErrorProducer = pongErrorProducer;
    this.dlqProducer = dlqProducer;
  }

  @Override
  public void pong(Message message, Duration duration) {
    pongSuccessProducer.apply(message, duration);
  }

  @Override
  public void pongForError(Message message) {
    pongErrorProducer.apply(message);
  }

  @Override
  public void dlq(Message message) {
    dlqProducer.apply(message);
  }
}
