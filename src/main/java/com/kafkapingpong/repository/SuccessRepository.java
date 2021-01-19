package com.kafkapingpong.repository;

import com.kafkapingpong.event.Message;

import java.time.Duration;

public interface SuccessRepository {
  void pong(Message pongMessage, Duration duration);
}
