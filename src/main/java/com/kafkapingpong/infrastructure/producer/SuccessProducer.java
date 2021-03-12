package com.kafkapingpong.infrastructure.producer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadSuccess;

import java.time.Duration;

public class SuccessProducer extends FunctionalMessageOutProducer {

  public void sendSuccess(Message message, Duration duration) {
    send(mapToPongSuccess(message, duration));
  }

  private MessageOut mapToPongSuccess(Message message, Duration duration) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadSuccess("pong", duration.toMillis()));
  }
}
