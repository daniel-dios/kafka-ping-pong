package com.kafkapingpong.usecase;

import java.time.Duration;
import java.util.UUID;

public interface ImageProcessor {
  Duration compute(UUID transactionId);
}
