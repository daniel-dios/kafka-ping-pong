package com.kafkapingpong.infrastructure.configuration;

import com.kafkapingpong.domain.message.MessageRepository;
import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.repository.MessageJDBCRepository;
import com.kafkapingpong.usecase.ImageProcessor;
import com.kafkapingpong.usecase.ImageProcessorFake;
import com.kafkapingpong.usecase.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class Config {

  @Bean
  public Processor processor(
      MessageRepository messageRepository,
      ImageProcessor imageProcessor,
      PongRepository pogRepository) {
    return new Processor(messageRepository, imageProcessor, pogRepository, 10);
  }

  @Bean
  public ImageProcessor imageProcessor() {
    return new ImageProcessorFake();
  }

  @Bean
  public MessageRepository messageRepository(
      NamedParameterJdbcTemplate jdbcTemplate) {
    return new MessageJDBCRepository(jdbcTemplate);
  }
}
