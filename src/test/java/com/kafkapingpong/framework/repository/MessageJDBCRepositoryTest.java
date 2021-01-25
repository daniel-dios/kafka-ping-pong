package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.Payload;
import com.kafkapingpong.framework.Application;
import com.kafkapingpong.framework.helper.DatabaseHelper;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static com.kafkapingpong.framework.helper.DockerComposeHelper.Compose.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
class MessageJDBCRepositoryTest {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  private MessageRepository repository;
  private DatabaseHelper helper;

  private static final DockerComposeHelper dockerCompose = new DockerComposeHelper(POSTGRES);

  @BeforeAll
  static void dockerComposeUp() {
    dockerCompose.start();
  }

  @AfterAll
  static void dockerComposeDown() {
    dockerCompose.stop();
  }

  @BeforeEach
  void setup() {
    repository = new MessageJDBCRepository(jdbcTemplate);
    helper = new DatabaseHelper(jdbcTemplate);
    helper.clean();
  }

  @Test
  void shouldStoreMessage() {
    final var expectedMessage = new Message(UUID.randomUUID(), new Payload("whateverMessage", false));

    repository.store(expectedMessage);

    assertThat(helper.getMessages()).containsExactly(expectedMessage);
  }

  @Test
  void shouldGetMessagesByInsertionOrder() {
    final var transactionId = UUID.randomUUID();
    final var expectedMessage = new Message(transactionId, new Payload("whateverMessage", false));
    final var lastMessage = new Message(transactionId, new Payload("whateverMessage", true));
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(new Message(UUID.randomUUID(), new Payload("whateverMessage", false)));
    repository.store(lastMessage);

    final var messages = repository.find(transactionId);

    assertThat(messages).hasSize(5);
    assertThat(messages.get(4)).isEqualTo(lastMessage);
  }
}
