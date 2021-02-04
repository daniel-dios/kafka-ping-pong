package com.kafkapingpong.end2end;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.helper.DatabaseHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaProducerHelper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.PONG_ERROR;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.PONG_TOPIC;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class EndToEndTest {
  private static final String PING_TOPIC = "ping";
  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final byte[] ERROR_MESSAGE = resourceToBytes("classpath:/examples/error-message.json");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final UUID TRANSACTION_ID = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");
  private static final Message PING_SUCCESS = new Message(TRANSACTION_ID, new Payload("ping", false));
  private static final int MAXIMUM_CONSECUTIVE_ATTEMPTS = 10;
  private final DatabaseHelper helper = getDatabaseHelper();

  private static final DockerComposeHelper dockerCompose = new DockerComposeHelper();

  @BeforeAll
  static void dockerComposeUp() {
    dockerCompose.start();
  }

  @AfterAll
  static void dockerComposeDown() {
    dockerCompose.stop();
  }

  @BeforeEach
  void beforeAll() {
    helper.clean();
  }

  private DatabaseHelper getDatabaseHelper() {
    var config = new HikariConfig();
    config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
    config.setUsername("postgres");
    config.setPassword("mysecretpassword");
    config.setDriverClassName(org.postgresql.Driver.class.getName());
    return new DatabaseHelper(new NamedParameterJdbcTemplate(new HikariDataSource(config)));
  }

  @Test
  void shouldBeIdempotent() throws Exception {
    final var KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();

    final var out = new KafkaConsumerHelper(List.of(PONG_TOPIC));
    out.consumeAll();
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verifyPongMessageWasProduced(out);

    out.consumeAll();
    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verifyPongMessageWasProducedWithLessDuration(out);

    assertThat(helper.getMessages()).containsExactly(PING_SUCCESS);
  }

  @Test
  void shouldConsumeError() throws Exception {
    final var KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();
    final var errorOut = new KafkaConsumerHelper(List.of(PONG_ERROR));
    errorOut.consumeAll();

    KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(ERROR_MESSAGE, UTF_8));

    verifyMessageWasProducedToErrorTopic(errorOut);

    assertThat(helper.getMessages())
        .containsExactly(new Message(TRANSACTION_ID, new Payload("ping", true)));
  }

  @Test
  void shouldProduceToDlqAfterMaximumConfigured() throws Exception {
    final var KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();
    final var errorOut = new KafkaConsumerHelper(List.of(PONG_ERROR));
    errorOut.consumeAll();

    final var messagesSent = sendElevenErrorMessages(KAFKA_PRODUCER_HELPER);

    verifyMessageWasProducedToDlq(new KafkaConsumerHelper(List.of("dlq")));

    assertThat(helper.getMessages()).containsExactlyElementsOf(messagesSent);
  }

  private List<Message> sendElevenErrorMessages(KafkaProducerHelper KAFKA_PRODUCER_HELPER) throws Exception {
    final var list = new ArrayList<Message>();
    for (int i = 0; i <= MAXIMUM_CONSECUTIVE_ATTEMPTS; i++) {
      KAFKA_PRODUCER_HELPER.send(PING_TOPIC, new String(ERROR_MESSAGE, UTF_8));
      list.add(new Message(TRANSACTION_ID, new Payload("ping", true)));
    }
    return list;
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

  private void verifyMessageWasProducedToDlq(KafkaConsumerHelper errorOut) {
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

  private void verifyPongMessageWasProduced(KafkaConsumerHelper out) {
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
