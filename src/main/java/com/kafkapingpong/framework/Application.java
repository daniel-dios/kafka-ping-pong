package com.kafkapingpong.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {
    "com.kafkapingpong.framework.configuration"
})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
