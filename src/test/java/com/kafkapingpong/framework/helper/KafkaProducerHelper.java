package com.kafkapingpong.framework.helper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;

public class KafkaProducerHelper {

  private final Producer<String, String> producer;

  public KafkaProducerHelper(String host, int port) {
    final var config = new HashMap<String, Object>();
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
    producer = new KafkaProducer<>(config);
  }

  public void send(String topic, String body) throws Exception {
    producer.send(new ProducerRecord<>(topic, body)).get();
    producer.flush();
  }
}
