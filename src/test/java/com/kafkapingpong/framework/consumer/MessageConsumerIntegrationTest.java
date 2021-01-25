package com.kafkapingpong.framework.consumer;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.Application;
import com.kafkapingpong.framework.helper.DatabaseHelper;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.helper.kafka.KafkaProducerHelper;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.BOTH;
import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class MessageConsumerIntegrationTest {

  @Autowired
  NamedParameterJdbcTemplate jdbcTemplate;

  private static final DockerComposeHelper dockerComposeHelper = new DockerComposeHelper(BOTH);

  final byte[] successMessage = resourceToBytes("classpath:/examples/success-message.json");
  final UUID transactionId = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");
  final Message message = new Message(transactionId, new Payload("ping", false));

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
    final var kafkaProducerHelper = new KafkaProducerHelper();
    final var databaseHelper = new DatabaseHelper(jdbcTemplate);
    databaseHelper.clean();

    kafkaProducerHelper.send("ping", new String(successMessage, UTF_8));

    databaseHelper.getMessages();
    await()
        .atMost(Duration.TWO_SECONDS)
        .pollDelay(Duration.FIVE_HUNDRED_MILLISECONDS)
        .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
        .until(() -> isaBoolean(databaseHelper.getMessages()));
  }

  private boolean isaBoolean(List<Message> messages) {
    return messages.size() == 1 && messages.get(0).equals(message);
  }
}
