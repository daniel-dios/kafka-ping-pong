package com.kafkapingpong.infrastructure.producer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadError;

public class ErrorProducer extends FunctionalMessageOutProducer {

  public void sendError(Message message) {
    send(mapToPongError(message));
  }

  private MessageOut mapToPongError(Message message) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadError("ping", message.isError()));
  }
}
