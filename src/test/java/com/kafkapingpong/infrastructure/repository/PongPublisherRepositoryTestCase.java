package com.kafkapingpong.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.Application;
import com.kafkapingpong.infrastructure.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.infrastructure.repository.dto.MessageOut;
import com.kafkapingpong.infrastructure.repository.dto.PayloadError;
import com.kafkapingpong.infrastructure.repository.dto.PayloadSuccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.UUID;

import static com.kafkapingpong.infrastructure.helper.kafka.KafkaConstants.DLQ;
import static com.kafkapingpong.infrastructure.helper.kafka.KafkaConstants.PONG_ERROR;
import static com.kafkapingpong.infrastructure.helper.kafka.KafkaConstants.PONG_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@DirtiesContext(classMode = AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public abstract class PongPublisherRepositoryTestCase {

  private static final UUID transactionId = UUID.randomUUID();
  private static final Duration DURATION = Duration.ofSeconds(31);

  private static final Message message = new Message(transactionId, new Payload("ping", false));
  private static final PayloadSuccess PAYLOAD_SUCCESS = new PayloadSuccess("pong", DURATION.toMillis());
  private static final MessageOut EXPECTED_PONG_SUCCESS = new MessageOut(transactionId.toString(), PAYLOAD_SUCCESS);

  private static final Message errorMessage = new Message(transactionId, new Payload("ping", true));
  private static final PayloadError PAYLOAD_ERROR = new PayloadError("ping", true);
  private static final MessageOut EXPECTED_PONG_ERROR = new MessageOut(transactionId.toString(), PAYLOAD_ERROR);

  private final KafkaConsumerHelper KAFKA_CONSUMER_HELPER = new KafkaConsumerHelper();

  @BeforeEach
  void setUp() {
    KAFKA_CONSUMER_HELPER.consumeAll();
  }

  @Autowired
  PongRepository pongRepository;

  @Test
  void shouldProduceSuccessEventToPong() throws JsonProcessingException {
    pongRepository.pong(message, DURATION);

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(1, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo(PONG_TOPIC);
    assertThat(all.get(0).value()).isEqualTo(new ObjectMapper().writeValueAsString(EXPECTED_PONG_SUCCESS));
  }

  @Test
  void shouldProduceErrorEventToPongError() throws JsonProcessingException {
    pongRepository.pongForError(errorMessage);

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(1, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo(PONG_ERROR);
    assertThat(all.get(0).value()).isEqualTo(new ObjectMapper().writeValueAsString(EXPECTED_PONG_ERROR));
  }

  @Test
  void shouldProduceErrorEventToDlq() throws JsonProcessingException {
    pongRepository.dlq(errorMessage);

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(1, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo(DLQ);
    assertThat(all.get(0).value()).isEqualTo(new ObjectMapper().writeValueAsString(EXPECTED_PONG_ERROR));
  }
}
