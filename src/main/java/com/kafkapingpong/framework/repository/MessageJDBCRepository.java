package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.Payload;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

public class MessageJDBCRepository implements MessageRepository {

  private static final String SELECT_TRANSACTION_ID_MESSAGE_ERROR_FROM_MESSAGES_WHERE_TRANSACTION_ID_TRANSACTION_ID =
      """
          SELECT transaction_id, message, error 
          FROM messages 
          WHERE transaction_id = :transactionId
          ORDER BY id DESC
          """;
  private static final String INSERT_INTO_MESSAGES_TRANSACTION_ID_MESSAGE_ERROR_VALUES_TRANSACTION_ID_MESSAGE_ERROR =
      """
          INSERT INTO messages(transaction_id, message, error) 
          VALUES (:transactionId, :message, :error)
          """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public MessageJDBCRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Message> find(UUID transactionId) {
    return jdbcTemplate.query(
        SELECT_TRANSACTION_ID_MESSAGE_ERROR_FROM_MESSAGES_WHERE_TRANSACTION_ID_TRANSACTION_ID,
        new MapSqlParameterSource("transactionId", transactionId),
        (rs, rw) ->
            new Message(
                rs.getObject("transaction_id", UUID.class),
                new Payload(
                    rs.getString("message"),
                    rs.getBoolean("error"))
            ));
  }

  @Override
  public void store(Message message) {
    final var paramSource = new MapSqlParameterSource();
    paramSource.addValue("transactionId", message.getTransactionId());
    paramSource.addValue("message", message.getMessage());
    paramSource.addValue("error", message.isError());

    jdbcTemplate
        .update(INSERT_INTO_MESSAGES_TRANSACTION_ID_MESSAGE_ERROR_VALUES_TRANSACTION_ID_MESSAGE_ERROR, paramSource);
  }
}
