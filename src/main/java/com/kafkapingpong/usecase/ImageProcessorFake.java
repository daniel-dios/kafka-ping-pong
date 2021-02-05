package com.kafkapingpong.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class ImageProcessorFake implements ImageProcessor {
  private final Logger logger = LoggerFactory.getLogger(ImageProcessorFake.class);

  @Override
  public Duration compute(UUID transactionId) {
    final var duration = Duration.ofSeconds(30).plusSeconds(new Random().nextInt(30));
    logger.info("{} computed in {} seconds", transactionId, duration.getSeconds());
    return duration;
  }
}
