package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.Application;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
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
import java.util.UUID;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.BOTH;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class PongPublisherRepositoryTest {

  private static final DockerComposeHelper DOCKER_COMPOSE_HELPER = new DockerComposeHelper(BOTH);
  private static final String TOPIC = "pong";
  private static final KafkaConsumerHelper KAFKA_CONSUMER_HELPER = new KafkaConsumerHelper(TOPIC);
  private static final UUID transactionId = UUID.randomUUID();
  private static final String EXPECTED_JSON = format("""
      {"transaction-id":"%s","payload":{"message":"pong","processing_time":31000}}
      """, transactionId.toString());
  private static final Message message = new Message(transactionId, new Payload(TOPIC, false));

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
  void shouldProduceSuccessEventToPong() {
    pongRepository.pong(message, Duration.ofSeconds(31));

    final var all = KAFKA_CONSUMER_HELPER.consumeAtLeast(1, Duration.ofSeconds(2)).findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).topic()).isEqualTo("pong");
    assertThat(all.get(0).value()).isEqualTo(EXPECTED_JSON);
  }
}
