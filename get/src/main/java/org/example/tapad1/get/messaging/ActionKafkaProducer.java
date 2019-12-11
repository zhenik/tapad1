package org.example.tapad1.get.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.tapad1.get.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionKafkaProducer {
  private static final Logger log = LoggerFactory.getLogger(ActionKafkaProducer.class);
  private final Producer<String, String> kProducer;
  private final String topic;

  public ActionKafkaProducer(Config config) {
    this.topic = config.topic;
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, config.name);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // order does not matter
    // throughput, latency
    props.put(ProducerConfig.LINGER_MS_CONFIG, 30);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384 * 5); // default x5
    props.put(ProducerConfig.ACKS_CONFIG, 1); // only master, no replicas
    this.kProducer = new KafkaProducer<String, String>(props);
  }

  public void send(String timestamp, String value) {
    final ProducerRecord<String, String> record = new ProducerRecord<>(topic, timestamp, value);
    kProducer.send(
        record,
        ((metadata, exception) -> {
          if (exception == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("topic", metadata.topic());
            data.put("partition", metadata.partition());
            data.put("offset", metadata.offset());
            data.put("timestamp", metadata.timestamp());
            log.info(data.toString());
          } else {
            exception.printStackTrace();
          }
        }));
  }
}
