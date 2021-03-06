package com.kafkapingpong.infrastructure.repository;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.MessageRepository;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.infrastructure.Application;
import com.kafkapingpong.infrastructure.helper.DatabaseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@DirtiesContext(classMode = AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public abstract class MessageJDBCRepositoryTestCase {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  private MessageRepository repository;
  private DatabaseHelper helper;

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
  void shouldGetMessagesFirstInLastOut() {
    final var transactionId = UUID.randomUUID();
    final var expectedMessage = new Message(transactionId, new Payload("whateverMessage", false));
    final var lastMessage = new Message(transactionId, new Payload("whateverMessage", true));
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(expectedMessage);
    repository.store(new Message(UUID.randomUUID(), new Payload("whateverMessage", false)));
    repository.store(lastMessage);

    final var messages = repository.getLast(transactionId, 4);

    assertThat(messages).hasSize(4);
    assertThat(messages.get(0)).isEqualTo(lastMessage);
    assertThat(messages.get(1)).isEqualTo(expectedMessage);
    assertThat(messages.get(2)).isEqualTo(expectedMessage);
    assertThat(messages.get(3)).isEqualTo(expectedMessage);
  }
}
