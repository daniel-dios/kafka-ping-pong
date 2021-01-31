package com.kafkapingpong.framework.publisher;

public class PongSuccess {
  private final String to;
  private final String text;

  public PongSuccess(String to, String text) {
    this.to = to;
    this.text = text;
  }

  @Override
  public String toString() {
    return text + to;
  }
}
