package com.kafkapingpong.framework.helper;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.io.File;

import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.KAFKA_PORT;
import static java.lang.String.valueOf;
import static java.lang.System.setProperty;
import static org.testcontainers.containers.wait.strategy.Wait.forListeningPort;
import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;
import static org.testcontainers.containers.wait.strategy.WaitAllStrategy.Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY;

public class DockerComposeHelper extends DockerComposeContainer<DockerComposeHelper> {
  private static final String POSTGRES = "postgres";
  private static final int POSTGRES_PORT = 5432;

  private static final String KAFKA = "kafka";

  private static final String ZOOKEEPER = "zookeeper";
  private static final int ZOOKEEPER_PORT = 2181;

  public DockerComposeHelper() {
    super(new File("docker-compose.yml"));

    this
        .withLocalCompose(true)
        .withExposedService(POSTGRES, POSTGRES_PORT)
        .waitingFor(
            POSTGRES,
            new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
                .withStrategy(forListeningPort())
                .withStrategy(forLogMessage(".*database system is ready to accept connections.*", 1)))
        .withExposedService(KAFKA, KAFKA_PORT)
        .waitingFor(
            KAFKA,
            new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
                .withStrategy(forListeningPort())
                .withStrategy(forLogMessage(".*creating topics.*", 1)))
        .withExposedService(ZOOKEEPER, ZOOKEEPER_PORT)
        .waitingFor(
            ZOOKEEPER,
            new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
                .withStrategy(forListeningPort())
                .withStrategy(forLogMessage(".*binding to port.*", 1)));
  }

  @Override
  public void start() {
    super.start();
    setSystemProperties();
  }

  private void setSystemProperties() {
    setProperty("db.host", this.getServiceHost(POSTGRES, POSTGRES_PORT));
    setProperty("db.port", valueOf(this.getServicePort(POSTGRES, POSTGRES_PORT)));

    setProperty("kafka.host", this.getServiceHost(KAFKA, KAFKA_PORT));
    setProperty("kafka.port", valueOf(KAFKA_PORT));
  }
}
