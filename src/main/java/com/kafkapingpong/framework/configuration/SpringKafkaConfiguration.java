package com.kafkapingpong.framework.configuration;

import com.kafkapingpong.framework.repository.exception.DbException;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
public class SpringKafkaConfiguration {

  @Bean
  public ListenerContainerCustomizer<AbstractMessageListenerContainer> containerCustomizer() {
    return (container, dest, group) -> {
      final var backOff = new ExponentialBackOff(50, 5);
      final var errorHandler = new SeekToCurrentErrorHandler(null, backOff);
      errorHandler.setClassifications(Map.of(DbException.class, true), false);
      var props = container.getContainerProperties();
      props.setAckMode(ContainerProperties.AckMode.RECORD);
      container.setErrorHandler(errorHandler);
    };
  }
}
