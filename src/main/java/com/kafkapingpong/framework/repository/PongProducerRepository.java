package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;

import static java.lang.String.format;

public class PongProducerRepository implements PongRepository {
  public static final String PARTITION_KEY = "partitionKey";

  private final MessageChannel output;

  public PongProducerRepository(MessageChannel output) {
    this.output = output;
  }

  @Override
  public void pong(Message message, Duration duration) {

    final var stringMessage = MessageBuilder
        .withPayload(
            format(
                """
                    {
                      "id": "%s",
                      "payload": {
                        "message": "pong",
                        "processing_time": %d
                      }
                    }
                    """, message.getTransactionId(), duration.getSeconds()))
        .setHeader(PARTITION_KEY, message.getTransactionId().toString())
        .build();

    output.send(stringMessage);
  }

  @Override
  public void pongForError(Message message) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void dlq(Message message) {
    throw new UnsupportedOperationException("not implemented yet");
  }
}
