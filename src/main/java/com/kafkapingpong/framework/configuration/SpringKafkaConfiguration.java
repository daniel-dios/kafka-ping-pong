package com.kafkapingpong.framework.configuration;

import com.kafkapingpong.event.PongRepository;
import com.kafkapingpong.framework.repository.PongProducerRepository;
import com.kafkapingpong.framework.repository.exception.DbException;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
@EnableBinding({PongChannels.class})
public class SpringKafkaConfiguration {

  @Bean
  public ListenerContainerCustomizer<AbstractMessageListenerContainer> containerCustomizer() {
    return (container, dest, group) -> {
      final var errorHandler = new SeekToCurrentErrorHandler(null, new ExponentialBackOff(50, 5));
      errorHandler.setClassifications(Map.of(DbException.class, true), false);
      container.setErrorHandler(errorHandler);

      final var props = container.getContainerProperties();
      props.setAckMode(ContainerProperties.AckMode.RECORD);
    };
  }

  @Bean
  public PongRepository pongRepository(PongChannels channels) {
    return new PongProducerRepository(
        channels.getPongChannel(),
        channels.getPongErrorChannel(),
        channels.getDlqChannel());
  }
}
