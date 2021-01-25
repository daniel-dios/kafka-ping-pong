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

public class DockerComposeHelper {
  private static final String POSTGRES = "postgres";
  private static final int POSTGRES_PORT = 5432;

  private static final String KAFKA = "kafka";

  private static final String ZOOKEEPER = "zookeeper";
  private static final int ZOOKEEPER_PORT = 2181;

  private final DockerComposeContainer container;
  private final Compose compose;

  public DockerComposeHelper(Compose compose) {
    this.compose = compose;
    container = new DockerComposeContainer<>(new File(compose.getCompose())).withLocalCompose(true);

    if (compose.equals(Compose.POSTGRES) || compose.equals(Compose.BOTH)) {
      container
          .withExposedService(POSTGRES, POSTGRES_PORT)
          .waitingFor(POSTGRES, new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
              .withStrategy(forListeningPort())
              .withStrategy(forLogMessage(".*database system is ready to accept connections.*", 1))
          );
    }

    if (compose.equals(Compose.KAFKA) || compose.equals(Compose.BOTH)) {
      container
          .withExposedService(KAFKA, KAFKA_PORT)
          .waitingFor(KAFKA, new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
              .withStrategy(forListeningPort())
              .withStrategy(forLogMessage(".*creating topics.*", 1))
          )
          .withExposedService(ZOOKEEPER, ZOOKEEPER_PORT)
          .waitingFor(ZOOKEEPER, new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
              .withStrategy(forListeningPort())
              .withStrategy(forLogMessage(".*binding to port.*", 1))
          );
    }
  }

  public void start() {
    container.start();
    setSystemProperties();
  }

  public void stop() {
    container.stop();
  }

  private void setSystemProperties() {

    if (compose.equals(Compose.POSTGRES) || compose.equals(Compose.BOTH)) {
      setProperty("db.host", container.getServiceHost(POSTGRES, POSTGRES_PORT));
      setProperty("db.port", valueOf(container.getServicePort(POSTGRES, POSTGRES_PORT)));
    }
    if (compose.equals(Compose.KAFKA) || compose.equals(Compose.BOTH)) {
      setProperty("kafka.host", container.getServiceHost(KAFKA, KAFKA_PORT));
      setProperty("kafka.port", valueOf(KAFKA_PORT));
    }
  }

  public enum Compose {
    BOTH("docker-compose.yml"),
    KAFKA("docker-compose-kafka.yml"),
    POSTGRES("docker-compose-db.yml");

    private final String compose;

    Compose(String compose) {
      this.compose = compose;
    }

    public String getCompose() {
      return compose;
    }
  }
}
