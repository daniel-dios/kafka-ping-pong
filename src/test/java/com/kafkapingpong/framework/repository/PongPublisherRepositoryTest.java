package com.kafkapingpong.framework.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.Application;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.framework.repository.dto.error.PongError;
import com.kafkapingpong.framework.repository.dto.success.PongSuccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.BOTH;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class PongPublisherRepositoryTest {

  private static final DockerComposeHelper DOCKER_COMPOSE_HELPER = new DockerComposeHelper(BOTH);
  private static final UUID transactionId = UUID.randomUUID();
  private static final Duration DURATION = Duration.ofSeconds(31);

  private static final Message message = new Message(transactionId, new Payload("ping", false));
  private static final PongSuccess EXPECTED_PONG_SUCCESS = new PongSuccess(transactionId.toString(),
      new com.kafkapingpong.framework.repository.dto.success.Payload("pong", DURATION.toMillis()));

  private static final Message errorMessage = new Message(transactionId, new Payload("ping", true));
  private static final PongError EXPECTED_PONG_ERROR = new PongError(transactionId.toString(),
      new com.kafkapingpong.framework.repository.dto.error.Payload("ping", true));

  private final String PONG_TOPIC = "pong";
  private final String PONG_ERROR = "pong-error";
  private final KafkaConsumerHelper KAFKA_CONSUMER_HELPER = new KafkaConsumerHelper(List.of(PONG_TOPIC, PONG_ERROR));

  @BeforeAll
  static void beforeAll() {
    DOCKER_COMPOSE_HELPER.start();
  }

  @AfterAll
  static void afterAll() {
    DOCKER_COMPOSE_HELPER.stop();
  }

  @BeforeEach
  void setUp() {
    KAFKA_CONSUMER_HELPER.consumeAll();
  }

  @Autowired
  PongRepository pongRepository;

  @Test
  void shouldProduceSuccessEventToPong() throws JsonProcessingException {
    pongRepository.pong(message, DURATION);

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(10, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo(PONG_TOPIC);
    final var actual = new ObjectMapper().readValue(all.get(0).value(), PongSuccess.class);
    assertThat(actual).usingRecursiveComparison().isEqualTo(EXPECTED_PONG_SUCCESS);
  }

  @Test
  void shouldProduceErrorEventToPongError() throws JsonProcessingException {
    pongRepository.pongForError(errorMessage);

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(10, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo(PONG_ERROR);
    final var actual = new ObjectMapper().readValue(all.get(0).value(), PongError.class);
    assertThat(actual).usingRecursiveComparison().isEqualTo(EXPECTED_PONG_ERROR);
  }
}
