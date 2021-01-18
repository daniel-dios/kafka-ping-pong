package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

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
    processedRepository.find(processRequest.getTransactionType());

    processedRepository.store(new Message(processRequest.getTransactionType(), processRequest.isError()));
    final var compute = imageProcessor.compute(processRequest.getTransactionType());

    final var timeConsumed = Instant.now().minusMillis(beginning.toEpochMilli());
    final var totalElapsed = compute.plusMillis(timeConsumed.toEpochMilli());

    successRepository.pong(new PongMessage(processRequest.getTransactionType(), "pong", totalElapsed));
  }
}
