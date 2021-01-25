package com.kafkapingpong.event;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
  List<Message> find(UUID transactionId, int numberOfMessages);

  void store(Message message);
}
