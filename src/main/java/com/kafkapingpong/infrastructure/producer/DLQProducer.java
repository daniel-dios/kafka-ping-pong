package com.kafkapingpong.infrastructure.producer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

public class DLQProducer implements Supplier<Flux<MessageOut>> {

  private final Sinks.Many<MessageOut> sink = Sinks.many().unicast().onBackpressureBuffer();

  public void apply(Message message) {
    synchronized (sink) {
      sink.emitNext(mapToPongError(message), Sinks.EmitFailureHandler.FAIL_FAST);
    }
  }

  private MessageOut mapToPongError(Message message) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadError("ping", message.isError()));
  }

  @Override
  public Flux<MessageOut> get() {
    return sink.asFlux();
  }
}
