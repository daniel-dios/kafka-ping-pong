package com.kafkapingpong.repository;

import com.kafkapingpong.event.ErrorPongMessage;

public interface ErrorRepository {
  void pongForError(ErrorPongMessage errorPongMessage);
}
