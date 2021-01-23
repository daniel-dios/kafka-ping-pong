package com.kafkapingpong.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class ImageProcessorTest {

  private final ImageProcessor imageProcessor = new ImageProcessor();

  @RepeatedTest(20)
  void shouldReturnGreaterThan30Seconds() {
    final var compute = imageProcessor.compute(randomUUID());
    System.out.println(compute.minus(Duration.ofSeconds(30)).getSeconds());

    assertThat(compute).isBetween(Duration.ofSeconds(30), Duration.ofMinutes(1));
  }
}
