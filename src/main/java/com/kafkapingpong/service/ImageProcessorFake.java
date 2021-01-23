package com.kafkapingpong.service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class ImageProcessorFake implements ImageProcessor {

  @Override
  public Duration compute(UUID transactionId) {
    return Duration.ofSeconds(30).plusSeconds(new Random().nextInt(30));
  }
}
