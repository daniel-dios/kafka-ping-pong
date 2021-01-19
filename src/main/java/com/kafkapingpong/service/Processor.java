package com.kafkapingpong.service;

import com.kafkapingpong.event.ErrorPongMessage;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongMessage;
import com.kafkapingpong.repository.ErrorRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;

public class Processor {

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
    var beginning = currentTimeMillis();
    final var id = processRequest.getTransactionId();
    final var messages = processedRepository.find(id);
    final var compact = compact(messages);

    if (compact.isEmpty() && processRequest.isError()) {
      processedRepository.store(new Message(id, processRequest.isError()));
      errorRepository.pongForError(new ErrorPongMessage(id, "pong", processRequest.isError()));
      return;
    }

    final var isExhausted = exhaustedAttempts(compact);
    if (processRequest.isError() && isExhausted) {
      // to dlq
      return;
    }

    if (processRequest.isError() && !isExhausted) {
      processedRepository.store(new Message(id, processRequest.isError()));
      errorRepository.pongForError(new ErrorPongMessage(id, "pong", processRequest.isError()));
      return;
    }

    if (!processRequest.isError() && !lastMessageWasSuccess(compact)) {
      final var computeTime = imageProcessor.compute(id);
      processedRepository.store(new Message(id, processRequest.isError()));
      successRepository.pong(new PongMessage(id, "pong", ofMillis(currentTimeMillis() - beginning).plus(computeTime)));
      return;
    }

    if (!processRequest.isError() && lastMessageWasSuccess(compact)) {
      successRepository.pong(new PongMessage(id, "pong", ofMillis(currentTimeMillis() - beginning)));
      return;
    }
  }

  private boolean exhaustedAttempts(List<Message> compact) {
    if (compact.isEmpty() || lastMessageWasSuccess(compact)) {
      return false;
    }
    var consecutiveAttempts = 0;
    for (int i = compact.size() - 1; i != 0; i--) {
      if (compact.get(i).isError()) {
        consecutiveAttempts++;
      }
      if (consecutiveAttempts >= attempts - 1) {
        return true;
      }
    }
    return false;
  }

  private boolean lastMessageWasSuccess(List<Message> compact) {
    if (compact.isEmpty()) {
      return false;
    }
    return !compact.get(compact.size() - 1).isError();
  }

  private List<Message> compact(List<Message> messages) {
    final var ret = new ArrayList<Message>();
    for (int i = 0; i < messages.size(); i++) {
      if (i >= messages.size() - attempts) {
        ret.add(messages.get(i));
      }
    }

    return ret;
  }

  private Duration calculateTimeElapsed(long beginning) {
    return ofMillis(currentTimeMillis() - beginning);
  }

  private void persist(ProcessRequest processRequest) {
    processedRepository.store(new Message(processRequest.getTransactionId(), processRequest.isError()));
  }
}
