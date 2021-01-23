package com.kafkapingpong.framework.configuration;

import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.framework.repository.MessageJDBCRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class Config {

  @Bean
  public MessageRepository messageRepository(NamedParameterJdbcTemplate jdbcTemplate){
    return new MessageJDBCRepository(jdbcTemplate);
  }
}
