package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.ErrorRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;

public class Processor {

  private final ProcessedRepository processedRepository;
  private final ImageProcessor imageProcessor;
  private final SuccessRepository successRepository;
  private final ErrorRepository errorRepository;
  private final int maxAttempts;

  public Processor(
      ProcessedRepository processedRepository,
      ImageProcessor imageProcessor,
      SuccessRepository successRepository,
      ErrorRepository errorRepository,
      int maxAttempts) {

    this.processedRepository = processedRepository;
    this.imageProcessor = imageProcessor;
    this.successRepository = successRepository;
    this.errorRepository = errorRepository;
    this.maxAttempts = maxAttempts;
  }

  public void process(ProcessRequest processRequest) {
    final var beginning = currentTimeMillis();
    final var id = processRequest.getTransactionId();
    final var compact = compact(processedRepository.find(id));

    if (processRequest.isError()) {
      if (!exhaustedAttempts(compact)) {
        processedRepository.store(new Message(id, processRequest.isError()));
        errorRepository.pongForError(new Message(id, processRequest.isError()));
      }
    } else if (!lastMessageWasSuccess(compact)) {
      final var computeTime = imageProcessor.compute(id);
      processedRepository.store(new Message(id, processRequest.isError()));
      successRepository.pong(new Message(id, processRequest.isError()), ofMillis(currentTimeMillis() - beginning).plus(computeTime));
    } else {
      successRepository.pong(new Message(id, processRequest.isError()), ofMillis(currentTimeMillis() - beginning));
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
      if (consecutiveAttempts >= maxAttempts - 1) {
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
      if (i >= messages.size() - maxAttempts) {
        ret.add(messages.get(i));
      }
    }
    return ret;
  }
}
