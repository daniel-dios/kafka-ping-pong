package com.kafkapingpong.infrastructure.configuration;

import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.consumer.MessageInConsumer;
import com.kafkapingpong.infrastructure.producer.MessageOutProducer;
import com.kafkapingpong.infrastructure.repository.PongProducerRepository;
import com.kafkapingpong.infrastructure.repository.exception.DbException;
import com.kafkapingpong.infrastructure.repository.exception.MessageNotSendException;
import com.kafkapingpong.usecase.Processor;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Configuration
public class CloudStreamConfiguration {

  @Bean
  public MessageOutProducer pongSuccessProducer() {
    return new MessageOutProducer();
  }

  @Bean
  public MessageOutProducer pongErrorProducer() {
    return new MessageOutProducer();
  }

  @Bean
  public MessageOutProducer dlqProducer() {
    return new MessageOutProducer();
  }

  @Bean
  public PongRepository pongRepository(
      MessageOutProducer pongSuccessProducer,
      MessageOutProducer pongErrorProducer,
      MessageOutProducer dlqProducer
  ) {
    return new PongProducerRepository(
        pongSuccessProducer,
        pongErrorProducer,
        dlqProducer);
  }

  @Bean
  public MessageInConsumer messageConsumer(Processor processor) {
    return new MessageInConsumer(processor);
  }

  @Bean
  public ListenerContainerCustomizer<AbstractMessageListenerContainer> containerCustomizer() {
    return (container, dest, group) -> {
      final var errorHandler = new SeekToCurrentErrorHandler(null, new ExponentialBackOff(50, 5));
      final Map<Class<? extends Throwable>, Boolean> repeatableExceptions = Map.of(
          DbException.class, true,
          MessageNotSendException.class, true);
      errorHandler.setClassifications(repeatableExceptions, false);
      container.setErrorHandler(errorHandler);

      final var props = container.getContainerProperties();
      props.setAckMode(ContainerProperties.AckMode.RECORD);
    };
  }
}
