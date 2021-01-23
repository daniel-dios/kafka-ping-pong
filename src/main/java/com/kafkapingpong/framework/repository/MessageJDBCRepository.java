package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

public class MessageJDBCRepository implements MessageRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public MessageJDBCRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Message> find(UUID transactionId) {
    throw new UnsupportedOperationException("not implemented yet ;)");
  }

  @Override
  public void store(Message message) {
    final var paramSource = new MapSqlParameterSource();
    paramSource.addValue("transactionId", message.getTransactionId());
    paramSource.addValue("message", message.getMessage());
    paramSource.addValue("error", message.isError());

    jdbcTemplate.update(
        "INSERT INTO messages(transaction_id, message, error) " +
            "VALUES (:transactionId, :message, :error)",
        paramSource);
  }
}
