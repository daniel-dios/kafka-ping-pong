package com.kafkapingpong.framework.helper.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;

import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.KAFKA_HOST;
import static com.kafkapingpong.framework.helper.kafka.KafkaConstants.KAFKA_PORT;

public class KafkaProducerHelper {

  private final Producer<String, String> producer;

  public KafkaProducerHelper() {
    final var config = new HashMap<String, Object>();
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_HOST + ":" + KAFKA_PORT);
    producer = new KafkaProducer<>(config);
  }

  public void send(String topic, String body) throws Exception {
    producer.send(new ProducerRecord<>(topic, body)).get();
    producer.flush();
  }
}
