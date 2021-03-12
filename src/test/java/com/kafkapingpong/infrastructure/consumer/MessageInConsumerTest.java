package com.kafkapingpong.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.infrastructure.consumer.dto.MessageIn;
import com.kafkapingpong.usecase.Processor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static com.kafkapingpong.infrastructure.helper.FileHelper.resourceToBytes;

public class MessageInConsumerTest {

  @Test
  void shouldConsumeBidCommand() throws Exception {
    final var successMessage = resourceToBytes("classpath:/examples/success-message.json");
    final var transactionId = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");
    final var success = new ObjectMapper().readValue(successMessage, MessageIn.class);
    final var processor = Mockito.mock(Processor.class);
    final var messageConsumer = new MessageInConsumer(processor);

    messageConsumer.accept(success);

    Mockito.verify(processor).process(new Message(transactionId, new Payload("ping", false)));
  }
}
