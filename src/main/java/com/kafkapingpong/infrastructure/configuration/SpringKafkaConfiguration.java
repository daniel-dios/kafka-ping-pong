package com.kafkapingpong.infrastructure.configuration;

import com.kafkapingpong.domain.message.PongRepository;
import com.kafkapingpong.infrastructure.consumer.MessageConsumer;
import com.kafkapingpong.infrastructure.producer.ErrorProducer;
import com.kafkapingpong.infrastructure.producer.SuccessProducer;
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
public class SpringKafkaConfiguration {

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

  @Bean
  public SuccessProducer pongSuccessProducer() {
    return new SuccessProducer();
  }

  @Bean
  public ErrorProducer pongErrorProducer() {
    return new ErrorProducer();
  }

  @Bean
  public ErrorProducer dlqProducer() {
    return new ErrorProducer();
  }

  @Bean
  public PongRepository pongRepository(
      SuccessProducer pongSuccessProducer,
      ErrorProducer pongErrorProducer,
      ErrorProducer dlqProducer
  ) {
    return new PongProducerRepository(
        pongSuccessProducer,
        pongErrorProducer,
        dlqProducer);
  }

  @Bean
  public MessageConsumer messageConsumer(
      Processor processor) {
    return new MessageConsumer(processor);
  }
}
