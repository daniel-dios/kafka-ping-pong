package com.kafkapingpong.infrastructure.repository;


import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.MessageRepository;
import com.kafkapingpong.domain.message.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

public class MessageJDBCRepository implements MessageRepository {
  private final Logger logger = LoggerFactory.getLogger(MessageJDBCRepository.class);

  private static final String SELECT_TRANSACTION_ID_MESSAGE_ERROR_FROM_MESSAGES_WHERE_TRANSACTION_ID_TRANSACTION_ID =
      """
          SELECT transaction_id, message, error 
          FROM messages 
          WHERE transaction_id = :transactionId
          ORDER BY id DESC
          LIMIT :limit
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
  public List<Message> getLast(UUID transactionId, int numberOfMessages) {
    final var paramSource = new MapSqlParameterSource("transactionId", transactionId);
    paramSource.addValue("limit", numberOfMessages);

    return jdbcTemplate.query(
        SELECT_TRANSACTION_ID_MESSAGE_ERROR_FROM_MESSAGES_WHERE_TRANSACTION_ID_TRANSACTION_ID,
        paramSource,
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

    final var update = jdbcTemplate
        .update(INSERT_INTO_MESSAGES_TRANSACTION_ID_MESSAGE_ERROR_VALUES_TRANSACTION_ID_MESSAGE_ERROR, paramSource);

    if (update > 0) {
      logger.info("{} saved as {} ", message.getTransactionId(), message.isError() ? "error" : "success");
    } else {
      logger.error("{} was not saved", message.getTransactionId());
    }
  }
}
