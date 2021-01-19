package com.kafkapingpong.repository;

import com.kafkapingpong.event.Message;

import java.time.Duration;

public interface PongRepository {
  void pong(Message pongMessage, Duration duration);

  void pongForError(Message errorPongMessage);
}
