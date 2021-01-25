package com.kafkapingpong.framework.repository;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongRepository;

import java.time.Duration;

public class VoidPongRepository implements PongRepository {
  @Override
  public void pong(Message message, Duration duration) {

  }

  @Override
  public void pongForError(Message message) {

  }

  @Override
  public void dlq(Message message) {

  }
}
