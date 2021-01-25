package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.MessageRepository;
import com.kafkapingpong.event.PongRepository;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;

public class Processor {

  private final MessageRepository messageRepository;
  private final ImageProcessor imageProcessor;
  private final PongRepository pongRepository;
  private final int maxAttempts;

  public Processor(
      MessageRepository messageRepository,
      ImageProcessor imageProcessor,
      PongRepository pongRepository,
      int maxAttempts) {
    this.messageRepository = messageRepository;
    this.imageProcessor = imageProcessor;
    this.pongRepository = pongRepository;
    this.maxAttempts = maxAttempts;
  }

  public void process(Message message) {
    var beginning = currentTimeMillis();
    final var messages = messageRepository.find(message.getTransactionId(), maxAttempts);

    if (message.isError() && !exhaustedAttempts(messages)) {
      messageRepository.store(message);
      pongRepository.pongForError(message);
      return;
    }

    if (message.isError() && exhaustedAttempts(messages)) {
      messageRepository.store(message);
      pongRepository.dlq(message);
      return;
    }

    if (!lastMessageWasSuccess(messages)) {
      beginning -= imageProcessor.compute(message.getTransactionId()).toMillis();
      messageRepository.store(message);
    }

    pongRepository.pong(message, ofMillis(currentTimeMillis() - beginning));
  }

  private boolean exhaustedAttempts(List<Message> compact) {
    if (compact.isEmpty() || lastMessageWasSuccess(compact)) {
      return false;
    }
    var consecutiveAttempts = 0;
    for (Message message : compact) {
      if (message.isError()) {
        consecutiveAttempts++;
        if (consecutiveAttempts >= maxAttempts) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean lastMessageWasSuccess(List<Message> compact) {
    if (compact.isEmpty()) {
      return false;
    }
    return !compact.get(0).isError();
  }
}
