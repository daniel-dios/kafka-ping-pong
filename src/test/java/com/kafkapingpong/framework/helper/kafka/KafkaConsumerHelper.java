package com.kafkapingpong.framework.helper.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaConsumerHelper {
  private static final long MILLIS_POLL = 250;

  private final Consumer<String, String> consumer;

  public KafkaConsumerHelper(String host, int port, String topic) {
    consumer = new KafkaConsumer<>(consumerConfig(host + ":" + port));
    consumer.subscribe(asList(topic));
  }

  public void consumeAll() {
    consumeAtLeast(1, ofSeconds(1));
  }

  public KafkaConsumerRecords consumeAtLeast(int numberOfRecords, Duration timeout) {
    KafkaConsumerRecords consumerRecords = new KafkaConsumerRecords();
    long millisLeft = timeout.toMillis();
    do {
      consumerRecords.add(consumer.poll(ofMillis(MILLIS_POLL)));
      millisLeft -= MILLIS_POLL;
    } while (millisLeft > 0 && consumerRecords.size() < numberOfRecords);
    return consumerRecords;
  }

  private Properties consumerConfig(String bootstrapServers) {
    Properties config = new Properties();
    config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST.name().toLowerCase());
    return config;
  }

  public static class KafkaConsumerRecords {

    private final List<ConsumerRecord<String, String>> records = new ArrayList<>();

    public int size() {
      return records.size();
    }

    public void add(ConsumerRecords<String, String> records) {
      records.iterator().forEachRemaining(this.records::add);
    }

    public ConsumerRecord<String, String> findOne(String key) {
      List<ConsumerRecord<String, String>> records = findAll(key);
      assertThat(records.size())
          .describedAs("findOne( key = %s ) returned zero or more than one record : available keys are [%s]", key,
              keysOf(findAll()))
          .isOne();
      return records.get(0);
    }

    public List<ConsumerRecord<String, String>> findAll() {
      return new ArrayList<>(records);
    }

    public List<ConsumerRecord<String, String>> findAll(String key) {
      return records.stream().filter(record -> key.equals(record.key())).collect(Collectors.toList());
    }

    private String keysOf(List<ConsumerRecord<String, String>> records) {
      return records.stream().map(ConsumerRecord::key).collect(Collectors.joining(","));
    }
  }
}
