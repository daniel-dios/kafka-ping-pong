package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.Payload;
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
    return jdbcTemplate.query(
        "SELECT transaction_id, message, error FROM messages WHERE transaction_id = :transactionId",
        new MapSqlParameterSource("transactionId", transactionId),
        (rs, rw) ->
            new Message(
                rs.getObject("transaction_id", UUID.class),
                new Payload(
                    rs.getString("message"),
                    rs.getBoolean("error")
                ))
    );
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
