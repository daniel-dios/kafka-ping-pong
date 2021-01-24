package com.kafkapingpong.framework.repository.helper;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.Payload;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

public class DatabaseHelper {
  private static final MapSqlParameterSource NO_PARAM = new MapSqlParameterSource();
  private final NamedParameterJdbcTemplate jdbcTemplate;

  public DatabaseHelper(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void clean() {
    jdbcTemplate.update("TRUNCATE TABLE messages", NO_PARAM);
  }

  public List<Message> getMessages() {
    return jdbcTemplate.query(
        "SELECT transaction_id, message, error FROM messages",
        NO_PARAM,
        (rs, rw) ->
            new Message(
                rs.getObject("transaction_id", UUID.class),
                new Payload(
                    rs.getString("message"),
                    rs.getBoolean("error")
                ))
    );
  }
}
