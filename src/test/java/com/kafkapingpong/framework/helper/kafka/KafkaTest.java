package com.kafkapingpong.framework.helper.kafka;

import com.kafkapingpong.framework.helper.DockerComposeHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
class KafkaTest {

  private static final String MESSAGE_CONTENT = """
      {"message": "content"}
              """;
  private static final String LOCALHOST = "localhost";
  private static final int PORT = 9094;
  private static final String TOPIC = "ping";
  private static final DockerComposeHelper dockerComposeHelper =
      new DockerComposeHelper(DockerComposeHelper.Compose.KAFKA);

  @BeforeAll
  static void beforeAll() {
    dockerComposeHelper.start();
  }

  @AfterAll
  static void afterAll() {
    dockerComposeHelper.stop();
  }

  @Test
  void shouldConsumeMessage() throws Exception {
    final var kafkaConsumerHelper = new KafkaConsumerHelper(Collections.singletonList(TOPIC));
    kafkaConsumerHelper.consumeAll();

    new KafkaProducerHelper().send(TOPIC, MESSAGE_CONTENT);
    final var records = kafkaConsumerHelper.consumeAtLeast(1, Duration.ofSeconds(1));
    final var all = records.findAll();

    assertThat(all).hasSize(1);
    assertThat(all.get(0).value()).isEqualTo(MESSAGE_CONTENT);
  }
}