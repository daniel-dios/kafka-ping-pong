package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.service.dto.ProcessRequest;

public class Processor {

  private final ProcessedRepository processedRepository;

  public Processor(ProcessedRepository processedRepository) {
    this.processedRepository = processedRepository;
  }

  public void process(ProcessRequest processRequest) {
    final var message = processedRepository.find(processRequest.getTransactionType());

    if (message.isEmpty()) {
      processedRepository.store(new Message(processRequest.getTransactionType(), processRequest.isError()));
    }
  }
}
