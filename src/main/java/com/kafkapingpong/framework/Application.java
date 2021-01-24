package com.kafkapingpong.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.kafkapingpong.framework.configuration"
})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
