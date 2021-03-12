package com.kafkapingpong.infrastructure.producer;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadSuccess;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.function.Supplier;

public class PongSuccessProducer implements Supplier<Flux<MessageOut>> {

  private final Sinks.Many<MessageOut> sink = Sinks.many().unicast().onBackpressureBuffer();

  public void apply(Message message, Duration duration) {
    synchronized (sink) {
      sink.emitNext(mapToPongSuccess(message, duration), Sinks.EmitFailureHandler.FAIL_FAST);
    }
  }

  private MessageOut mapToPongSuccess(Message message, Duration duration) {
    return new MessageOut(
        message.getTransactionId().toString(),
        new PayloadSuccess("pong", duration.toMillis()));
  }

  @Override
  public Flux<MessageOut> get() {
    return sink.asFlux();
  }
}
