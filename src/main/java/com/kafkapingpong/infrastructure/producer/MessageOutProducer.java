package com.kafkapingpong.infrastructure.producer;

import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

public class MessageOutProducer implements Supplier<Flux<MessageOut>> {
  private final Sinks.Many<MessageOut> sink = Sinks.many().unicast().onBackpressureBuffer();

  public void send(MessageOut out) {
    synchronized (sink) {
      sink.emitNext(out, Sinks.EmitFailureHandler.FAIL_FAST);
    }
  }

  @Override
  public Flux<MessageOut> get() {
    return sink.asFlux();
  }
}
