package com.kafkapingpong.repository;


import com.kafkapingpong.event.Message;

import java.util.List;
import java.util.UUID;

public interface ProcessedRepository {
  List<Message> find(UUID transactionId);

  void store(Message message);
}
