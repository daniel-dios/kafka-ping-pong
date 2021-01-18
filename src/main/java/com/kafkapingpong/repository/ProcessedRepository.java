package com.kafkapingpong.repository;


import com.kafkapingpong.event.Message;

import java.util.List;
import java.util.UUID;

public class ProcessedRepository {
  public List<Message> find(UUID transactionId) {
    throw new UnsupportedOperationException();
  }

  public void store(Message message) {
    throw new UnsupportedOperationException();
  }
}
