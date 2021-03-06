package com.kafkapingpong.infrastructure.helper.kafka;

import com.kafkapingpong.infrastructure.helper.DockerComposeHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@DirtiesContext(classMode = AFTER_CLASS)
class KafkaTest {

  private static final String MESSAGE_CONTENT = """
      {"message": "content"}
              """;
  private static final String TOPIC = "ping";

  @Disabled
  @Test
  void shouldConsumeMessage() throws Exception {
    final var dockerComposeHelper = new DockerComposeHelper();
    dockerComposeHelper.start();
    final var kafkaConsumerHelper = new KafkaConsumerHelper();
    kafkaConsumerHelper.consumeAll();

    new KafkaProducerHelper().send(TOPIC, MESSAGE_CONTENT);

    final var records = kafkaConsumerHelper.consumeAtLeast(1, Duration.ofSeconds(1));
    final var all = records.findAll();

    assertThat(all).hasSize(1);
    assertThat(all.get(0).value()).isEqualTo(MESSAGE_CONTENT);

    dockerComposeHelper.stop();
  }
}