package com.kafkapingpong.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.helper.DatabaseHelper;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaProducerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.BOTH;
import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.PONG_TOPIC;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {
  private static final DockerComposeHelper DOCKER_COMPOSE_HELPER = new DockerComposeHelper(BOTH);
  private static final KafkaProducerHelper KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();
  private static final String PING_TOPIC = "ping";
  private static final KafkaConsumerHelper KAFKA_CONSUMER_HELPER = new KafkaConsumerHelper();
  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final UUID TRANSACTION_ID = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  private DatabaseHelper helper;

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
    helper = new DatabaseHelper(jdbcTemplate);
    helper.clean();
    KAFKA_CONSUMER_HELPER.consumeAll();
  }

  @Test
  void shouldProduceOutMessageWhenSuccess() throws Exception {
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    final var out = new KafkaConsumerHelper(List.of(PONG_TOPIC));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              final var all = out.consumeAtLeast(1, Duration.ofSeconds(1)).findAll();
              return all.size() == 1 && isExpectedProcessedMessage(all.get(0).value());
            });

    assertThat(helper.getMessages()).containsExactly(new Message(TRANSACTION_ID, new Payload("ping", false)));
  }

  private boolean isExpectedProcessedMessage(String value) throws JsonProcessingException {
    final var jsonNode = OBJECT_MAPPER.readTree(value);
    final var tId = jsonNode.get("transaction-id").asText();
    final var payload = jsonNode.get("payload");
    final var message = payload.get("message").textValue();
    final var duration = payload.get("processing_time").asLong();
    return tId.equals(TRANSACTION_ID.toString())
        && "pong".equals(message)
        && duration > Duration.ofSeconds(30).toMillis();
  }
}
