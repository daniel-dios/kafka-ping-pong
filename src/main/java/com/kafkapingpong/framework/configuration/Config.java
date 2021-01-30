package com.kafkapingpong.framework.configuration;

import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.consumer.MessageConsumer;
import com.kafkapingpong.framework.repository.MessageJDBCRepository;
import com.kafkapingpong.service.ImageProcessor;
import com.kafkapingpong.service.ImageProcessorFake;
import com.kafkapingpong.service.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class Config {

  @Bean
  public MessageConsumer messageConsumer(
      Processor processor) {
    return new MessageConsumer(processor);
  }

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
