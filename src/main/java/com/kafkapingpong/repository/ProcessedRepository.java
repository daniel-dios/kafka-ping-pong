package com.kafkapingpong.repository;


import com.kafkapingpong.event.Message;

import java.util.Optional;
import java.util.UUID;

public class ProcessedRepository {
  public Optional<Message> find(UUID uuid) {
    throw new UnsupportedOperationException();
  }

  public void store(Message message) {
    throw new UnsupportedOperationException();
  }
}
