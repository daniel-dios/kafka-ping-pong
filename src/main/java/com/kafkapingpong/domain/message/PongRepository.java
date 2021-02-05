package com.kafkapingpong.domain.message;

import java.time.Duration;

public interface PongRepository {
  void pong(Message message, Duration duration);

  void pongForError(Message message);

  void dlq(Message message);
}
