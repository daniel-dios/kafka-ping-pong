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

import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.KAFKA_HOST;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.KAFKA_PORT;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.TOPICS;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;

public class KafkaConsumerHelper {
  private static final long MILLIS_POLL = 250;

  private final Consumer<String, String> consumer;

  public KafkaConsumerHelper() {
    consumer = new KafkaConsumer<>(consumerConfig());
    consumer.subscribe(TOPICS);
  }

  public KafkaConsumerHelper(List<String> topics) {
    consumer = new KafkaConsumer<>(consumerConfig());
    consumer.subscribe(topics);
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

  private Properties consumerConfig() {
    Properties config = new Properties();
    config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_HOST + ":" + KAFKA_PORT);
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

    public List<ConsumerRecord<String, String>> findAll() {
      return new ArrayList<>(records);
    }
  }
}
