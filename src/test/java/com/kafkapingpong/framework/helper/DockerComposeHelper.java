package com.kafkapingpong.framework.helper;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.io.File;

import static java.lang.String.valueOf;
import static java.lang.System.setProperty;
import static org.testcontainers.containers.wait.strategy.Wait.forListeningPort;
import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;
import static org.testcontainers.containers.wait.strategy.WaitAllStrategy.Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY;

public class DockerComposeHelper {
  private static final String POSTGRES = "postgres";
  private static final int POSTGRES_PORT = 5432;

  private final DockerComposeContainer container;

  public DockerComposeHelper() {
    container = new DockerComposeContainer(new File("docker-compose.yml"))
        .withLocalCompose(true)
        .withExposedService(POSTGRES, POSTGRES_PORT)
        .waitingFor(POSTGRES, new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
            .withStrategy(forListeningPort())
            .withStrategy(forLogMessage(".*database system is ready to accept connections.*", 1))
        );
  }

  public void start() {
    container.start();
    setSystemProperties();
  }

  public void stop() {
    container.stop();
  }

  private void setSystemProperties() {
    setProperty("db.host", container.getServiceHost(POSTGRES, POSTGRES_PORT));
    setProperty("db.port", valueOf(container.getServicePort(POSTGRES, POSTGRES_PORT)));
  }
}
