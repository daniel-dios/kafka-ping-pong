package com.kafkapingpong.repository;

import com.kafkapingpong.event.PongMessage;

public interface SuccessRepository {
  void pong(PongMessage pongMessage);
}
