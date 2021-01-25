package com.kafkapingpong.framework.consumer.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static com.kafkapingpong.framework.helper.FileHelper.resourceToBytes;
import static org.assertj.core.api.Assertions.assertThat;

class MessageInTest {

  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final byte[] ERROR_MESSAGE = resourceToBytes("classpath:/examples/error-message.json");
  private static final UUID TRANSACTION_ID = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");

  @Test
  void shouldParseMessage() throws IOException {
    var success = new ObjectMapper().readValue(SUCCESS_MESSAGE, MessageIn.class);
    var error = new ObjectMapper().readValue(ERROR_MESSAGE, MessageIn.class);

    assertThat(success.id).isEqualTo(TRANSACTION_ID);
    assertThat(success.payload.message).isEqualTo("ping");
    assertThat(success.payload.forceError).isFalse();

    assertThat(error.id).isEqualTo(TRANSACTION_ID);
    assertThat(error.payload.message).isEqualTo("ping");
    assertThat(error.payload.forceError).isTrue();
  }
}
