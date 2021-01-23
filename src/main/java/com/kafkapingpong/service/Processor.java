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
    final var compact = compact(messageRepository.find(message.getTransactionId()));

    if (message.isError() && !exhaustedAttempts(compact)) {
      messageRepository.store(message);
      pongRepository.pongForError(message);
      return;
    }

    if (message.isError() && exhaustedAttempts(compact)) {
      messageRepository.store(message);
      pongRepository.dlq(message);
      return;
    }

    if (!lastMessageWasSuccess(compact)) {
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
