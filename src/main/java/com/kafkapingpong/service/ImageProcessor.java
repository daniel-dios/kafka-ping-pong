package com.kafkapingpong.service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class ImageProcessor {
  public Duration compute(UUID transactionId) {
    return Duration.ofSeconds(30).plusSeconds(new Random().nextInt(30));
  }
}
