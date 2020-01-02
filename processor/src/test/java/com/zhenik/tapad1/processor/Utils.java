package com.zhenik.tapad1.processor;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

public class Utils {
  static Properties getProperties(String clientId, String kafkaBootstrapServers) {
      final Properties streamsConfiguration = new Properties();
      streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "analytics-processor-app-id");
      streamsConfiguration.put(
          StreamsConfig.CLIENT_ID_CONFIG, "analytics-processor-" + clientId);
      streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
      streamsConfiguration.put(
          StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
      streamsConfiguration.put(
          StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
      streamsConfiguration.setProperty(StreamsConfig.TOPOLOGY_OPTIMIZATION, StreamsConfig.OPTIMIZE);
      streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
      streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/streams-analytics");
      return streamsConfiguration;
  }
}
