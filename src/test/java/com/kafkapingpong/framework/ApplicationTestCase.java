package com.kafkapingpong.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.helper.DatabaseHelper;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaProducerHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.PONG_ERROR;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.PONG_TOPIC;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTestCase {
  private static final KafkaProducerHelper KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();
  private static final String PING_TOPIC = "ping";
  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final byte[] ERROR_MESSAGE = resourceToBytes("classpath:/examples/error-message.json");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final UUID TRANSACTION_ID = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");
  private static final Message PING_SUCCESS = new Message(TRANSACTION_ID, new Payload("ping", false));

  private static final DockerComposeHelper dockerCompose = new DockerComposeHelper();

  @BeforeAll
  static void dockerComposeUp() {
    dockerCompose.start();
  }

  @AfterAll
  static void dockerComposeDown() {
    dockerCompose.stop();
  }

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private MessageRepository messageRepository;

  private DatabaseHelper helper;

  @BeforeEach
  void setUp() {
    helper = new DatabaseHelper(jdbcTemplate);
    helper.clean();
  }

  @Test
  void shouldBeIdempotent() throws Exception {
    final var out = new KafkaConsumerHelper(List.of(PONG_TOPIC));
    out.consumeAll();
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verifyPongMessageWasProducced(out);

    out.consumeAll();
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verifyPongMessageWasProducedWithLessDuration(out);

    assertThat(helper.getMessages()).containsExactly(PING_SUCCESS);
  }

  @Test
  void shouldConsumeError() throws Exception {
    final var errorOut = new KafkaConsumerHelper(List.of(PONG_ERROR));
    errorOut.consumeAll();
    messageRepository.store(PING_SUCCESS);
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(ERROR_MESSAGE, UTF_8));

    verifyMessageWasProducedToErrorTopic(errorOut);

    assertThat(helper.getMessages())
        .containsExactly(PING_SUCCESS, new Message(TRANSACTION_ID, new Payload("ping", true)));
  }

  private void verifyMessageWasProducedToErrorTopic(KafkaConsumerHelper errorOut) {
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              final var all = errorOut.consumeAtLeast(1, Duration.ofSeconds(5)).findAll();
              return all.size() == 1;
            });
  }

  private void verifyPongMessageWasProducedWithLessDuration(KafkaConsumerHelper out) {
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              final var all = out.consumeAtLeast(1, Duration.ofSeconds(5)).findAll();
              return !all.isEmpty() && atLeastOneHasShortDuration(all);
            });
  }

  private void verifyPongMessageWasProducced(KafkaConsumerHelper out) {
    await().atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              final var all = out.consumeAtLeast(1, Duration.ofSeconds(5)).findAll();
              return !all.isEmpty() && successAndProcessed(all.get(0).value());
            });
  }

  private boolean atLeastOneHasShortDuration(List<ConsumerRecord<String, String>> all) {
    return all.stream().map(s -> getDuration(s.value())).anyMatch(dur -> dur < 30);
  }

  private boolean successAndProcessed(String value) throws JsonProcessingException {
    final var jsonNode = OBJECT_MAPPER.readTree(value);
    final var tId = jsonNode.get("transaction-id").asText();
    final var payload = jsonNode.get("payload");
    final var message = payload.get("message").textValue();
    final var duration = payload.get("processing_time").asLong();
    return tId.equals(TRANSACTION_ID.toString())
        && "pong".equals(message)
        && duration > Duration.ofSeconds(30).toMillis();
  }

  private long getDuration(String value) {
    try {
      final JsonNode jsonNode;
      jsonNode = OBJECT_MAPPER.readTree(value);
      final var payload = jsonNode.get("payload");
      return payload.get("processing_time").asLong();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unreachable code");
    }
  }
}
