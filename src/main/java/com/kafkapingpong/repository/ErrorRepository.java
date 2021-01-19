package com.kafkapingpong.repository;

import com.kafkapingpong.event.Message;

public interface ErrorRepository {
  void pongForError(Message errorPongMessage);
}
