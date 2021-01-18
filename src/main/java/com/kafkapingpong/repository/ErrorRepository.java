package com.kafkapingpong.repository;

import com.kafkapingpong.event.ErrorPongMessage;

public class ErrorRepository {
  public void pongForError(ErrorPongMessage errorPongMessage) {
    throw new UnsupportedOperationException();
  }
}
