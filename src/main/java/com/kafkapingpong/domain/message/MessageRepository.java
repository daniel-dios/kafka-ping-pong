package com.kafkapingpong.domain.message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
  List<Message> getLast(UUID transactionId, int numberOfMessages);

  void store(Message message);
}
