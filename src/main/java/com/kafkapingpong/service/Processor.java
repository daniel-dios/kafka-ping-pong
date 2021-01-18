package com.kafkapingpong.service;

import com.kafkapingpong.event.ErrorPongMessage;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongMessage;
import com.kafkapingpong.repository.ErrorRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

import java.time.Duration;
import java.time.Instant;

public class Processor {

  private static final String PONG = "pong";
  private final ProcessedRepository processedRepository;
  private final ImageProcessor imageProcessor;
  private final SuccessRepository successRepository;
  private final ErrorRepository errorRepository;
  private final int attempts;

  public Processor(
      ProcessedRepository processedRepository,
      ImageProcessor imageProcessor,
      SuccessRepository successRepository,
      ErrorRepository errorRepository,
      int attempts) {

    this.processedRepository = processedRepository;
    this.imageProcessor = imageProcessor;
    this.successRepository = successRepository;
    this.errorRepository = errorRepository;
    this.attempts = attempts;
  }

  public void process(ProcessRequest processRequest) {
    final var beginning = Instant.now();
    final var transactionId = processRequest.getTransactionId();
    final var message = processedRepository.find(transactionId);
    final Duration duration;

    if (processRequest.isError() && message.stream().filter(Message::isError).count() >= attempts) {
      return;
    }

    if (processRequest.isError()) {
      persist(processRequest);
      errorRepository.pongForError(new ErrorPongMessage(transactionId, PONG, true));
      return;
    }

    if (message.isEmpty()) {
      persist(processRequest);
      duration = imageProcessor.compute(transactionId);
    } else {
      duration = Duration.ofMinutes(0);
    }

    final var pongMessage = new PongMessage(transactionId, PONG, getDuration(beginning, duration));
    successRepository.pong(pongMessage);
  }

  private void persist(ProcessRequest processRequest) {
    processedRepository.store(new Message(processRequest.getTransactionId(), processRequest.isError()));
  }

  private Duration getDuration(Instant beginning, Duration duration) {
    return Duration.ofMillis(Instant.now().minusMillis(beginning.toEpochMilli()).toEpochMilli()).plus(duration);
  }
}
