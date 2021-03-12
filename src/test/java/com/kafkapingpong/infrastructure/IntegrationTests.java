package com.kafkapingpong.infrastructure;

import com.kafkapingpong.infrastructure.consumer.MessageInConsumerIntegrationTestCase;
import com.kafkapingpong.infrastructure.helper.DockerComposeHelper;
import com.kafkapingpong.infrastructure.repository.MessageJDBCRepositoryTestCase;
import com.kafkapingpong.infrastructure.repository.PongPublisherRepositoryTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

public class IntegrationTests {

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
  class PongPublisherRepositoryTest extends PongPublisherRepositoryTestCase {
  }

  @Nested
  class MessageJDBCRepositoryTest extends MessageJDBCRepositoryTestCase {
  }

  @Nested
  class MessageInConsumerIntegrationTest extends MessageInConsumerIntegrationTestCase {
  }

}
