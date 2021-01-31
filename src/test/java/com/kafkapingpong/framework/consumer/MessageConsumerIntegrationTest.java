package com.kafkapingpong.framework.consumer;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.Application;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaConsumerHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaProducerHelper;
import com.kafkapingpong.framework.repository.exception.DbException;
import com.kafkapingpong.service.Processor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.UUID;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.BOTH;
import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class MessageConsumerIntegrationTest {

  @MockBean
  Processor processor;

  private static final DockerComposeHelper DOCKER_COMPOSE_HELPER = new DockerComposeHelper(BOTH);
  private static final KafkaProducerHelper KAFKA_PRODUCER_HELPER = new KafkaProducerHelper();
  private static final String TOPIC = "ping";
  private static final KafkaConsumerHelper KAFKA_CONSUMER_HELPER = new KafkaConsumerHelper(
      Collections.singletonList(TOPIC));
  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final UUID transactionId = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");
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

  @Test
  void shouldCallOnceToProcessor() throws Exception {
    doNothing().when(processor).process(message);

    KAFKA_PRODUCER_HELPER.send(TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verify(processor, timeout(10000).times(1)).process(message);
  }

  @Test
  void shouldRetryWhenDbExceptionForever() throws Exception {
    doThrow(new DbException())
        .doThrow(new DbException())
        .doThrow(new DbException())
        .doNothing()
        .when(processor)
        .process(message);

    KAFKA_PRODUCER_HELPER.send(TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verify(processor, timeout(10000).atLeast(4)).process(message);
  }

  @Test
  void shouldRetryOnceWhenDifferentException() throws Exception {
    doThrow(new IllegalArgumentException())
        .doThrow(new IllegalArgumentException())
        .doThrow(new IllegalArgumentException())
        .doNothing()
        .when(processor)
        .process(message);

    KAFKA_PRODUCER_HELPER.send(TOPIC, new String(SUCCESS_MESSAGE, UTF_8));

    verify(processor, timeout(10000).times(1)).process(message);
  }
}
