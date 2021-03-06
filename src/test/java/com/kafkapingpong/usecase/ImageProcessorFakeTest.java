package com.kafkapingpong.usecase;

import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class ImageProcessorFakeTest {

  private final ImageProcessor imageProcessorFake = new ImageProcessorFake();

  @RepeatedTest(100)
  void shouldReturnGreaterThan30Seconds() {
    final var compute = imageProcessorFake.compute(randomUUID());

    assertThat(compute).isBetween(Duration.ofSeconds(30), Duration.ofMinutes(1));
  }
}
