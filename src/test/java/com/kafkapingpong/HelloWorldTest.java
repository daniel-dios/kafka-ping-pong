package com.kafkapingpong;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class HelloWorldTest {

  @Test
  void shouldAssertTrue() {
    Assertions.assertThat(1).isEqualTo(1);
  }
}
