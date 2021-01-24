package com.kafkapingpong.framework.consumer.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageInTest {

  private static final ResourceLoader resourceLoader = new DefaultResourceLoader();
  private static final byte[] SUCCESS_MESSAGE = resourceToBytes("classpath:/examples/success-message.json");
  private static final byte[] ERROR_MESSAGE = resourceToBytes("classpath:/examples/error-message.json");
  private static final UUID TRANSACTION_ID = UUID.fromString("9981f951-3ed7-46b7-8a23-86a87d9ffdaa");

  @Test
  void shouldParseMessage() throws IOException {
    var success = new ObjectMapper().readValue(SUCCESS_MESSAGE, MessageIn.class);
    var error = new ObjectMapper().readValue(ERROR_MESSAGE, MessageIn.class);

    assertThat(success.getId()).isEqualTo(TRANSACTION_ID);
    assertThat(success.getMessage()).isEqualTo("ping");
    assertThat(success.isForceError()).isFalse();

    assertThat(error.getId()).isEqualTo(TRANSACTION_ID);
    assertThat(error.getMessage()).isEqualTo("ping");
    assertThat(error.isForceError()).isTrue();
  }

  private static byte[] resourceToBytes(String path) {
    try (InputStream is = resourceLoader.getResource(path).getInputStream()) {
      return IOUtils.toByteArray(is);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
