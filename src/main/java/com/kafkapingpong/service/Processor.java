package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongMessage;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

import java.time.Duration;
import java.time.Instant;

public class Processor {

  private final ProcessedRepository processedRepository;
  private final ImageProcessor imageProcessor;
  private final SuccessRepository successRepository;

  public Processor(
      ProcessedRepository processedRepository,
      ImageProcessor imageProcessor,
      SuccessRepository successRepository) {

    this.processedRepository = processedRepository;
    this.imageProcessor = imageProcessor;
    this.successRepository = successRepository;
  }

  public void process(ProcessRequest processRequest) {
    final var beginning = Instant.now();
    final var message = processedRepository.find(processRequest.getTransactionType());
    final Duration duration;

    if (message.isPresent()) {
      duration = Duration.ofMinutes(0);
    } else {
      persist(processRequest);
      duration = imageProcessor.compute(processRequest.getTransactionType());
    }

    successRepository
        .pong(new PongMessage(processRequest.getTransactionType(), "pong", getDuration(beginning, duration)));
  }

  private void persist(ProcessRequest processRequest) {
    processedRepository.store(new Message(processRequest.getTransactionType(), processRequest.isError()));
  }

  private Duration getDuration(Instant beginning, Duration duration) {
    return Duration.ofMillis(Instant.now().minusMillis(beginning.toEpochMilli()).toEpochMilli()).plus(duration);
  }
}
