package com.zhenik.tapad1.command.messaging;

import com.zhenik.tapad1.command.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandKafkaProducer {
  private static final Logger log = LoggerFactory.getLogger(CommandKafkaProducer.class);
  private final Producer<String, String> kProducer;
  private final String topic;

  public CommandKafkaProducer(Config config) {
    this.topic = config.topic;
    //todo: extract props to config
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, config.name);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // throughput, latency
    props.put(ProducerConfig.LINGER_MS_CONFIG, 30);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384 * 5); // default x5
    props.put(ProducerConfig.ACKS_CONFIG, "1"); // only master, no replicas

    // delivery semantics
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // order does not matter
    props.put(ProducerConfig.RETRIES_CONFIG, 1000); // retries (less data loss, potential duplicates)
    //props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    //props.put(ProducerConfig.ACKS_CONFIG, "all"); // only master, no replicas

    this.kProducer = new KafkaProducer<>(props);
  }

  public void send(String timestamp, String user, String action) {
    final String value = user + " " + action;
    final ProducerRecord<String, String> record = new ProducerRecord<>(topic, timestamp, value);
    kProducer.send(
        record,
        ((metadata, exception) -> {
          if (exception == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("topic", metadata.topic());
            data.put("partition", metadata.partition());
            data.put("offset", metadata.offset());
            data.put("committed_timestamp", metadata.timestamp());
            log.info(data.toString());
          } else {
            exception.printStackTrace();
          }
        }));
  }
}
