package com.kafkapingpong.framework;

import com.kafkapingpong.framework.consumer.MessageConsumerIntegrationTestCase;
import com.kafkapingpong.framework.helper.DockerComposeHelper;
import com.kafkapingpong.framework.repository.MessageJDBCRepositoryTestCase;
import com.kafkapingpong.framework.repository.PongPublisherRepositoryTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

public class SpringTests {

  private static final DockerComposeHelper dockerCompose = new DockerComposeHelper();

  @BeforeAll
  static void dockerComposeUp() {
    dockerCompose.start();
  }

  @AfterAll
  static void dockerComposeDown() {
    dockerCompose.stop();
  }

  @Nested
  class PongPublisherRepository extends PongPublisherRepositoryTestCase {
  }

  @Nested
  class MessageJDBCRepositoryTest extends MessageJDBCRepositoryTestCase {
  }

  @Nested
  class MessageConsumerIntegrationTest extends MessageConsumerIntegrationTestCase {
  }

}
