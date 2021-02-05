package com.kafkapingpong.usecase;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.MessageRepository;
import com.kafkapingpong.domain.message.PongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;

public class Processor {

  private final Logger logger = LoggerFactory.getLogger(Processor.class);

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

  @Transactional
  public void process(Message message) {
    var beginning = currentTimeMillis();
    final var messages = messageRepository.getLast(message.getTransactionId(), maxAttempts);

    if (message.isError() && !exhaustedAttempts(messages)) {
      messageRepository.store(message);
      pongRepository.pongForError(message);
      logger.info("{} processed as error", message.getTransactionId());
      return;
    }

    if (message.isError() && exhaustedAttempts(messages)) {
      messageRepository.store(message);
      pongRepository.dlq(message);
      logger.info("{} processed as dlq-ed", message.getTransactionId());
      return;
    }

    if (!lastMessageWasSuccess(messages)) {
      beginning -= imageProcessor.compute(message.getTransactionId()).toMillis();
      messageRepository.store(message);
      logger.info("{} processed as success for first time", message.getTransactionId());
    }

    pongRepository.pong(message, ofMillis(currentTimeMillis() - beginning));
    logger.info("{} processed as success", message.getTransactionId());
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
