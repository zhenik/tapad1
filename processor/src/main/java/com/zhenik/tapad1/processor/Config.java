package com.zhenik.tapad1.processor;

import com.typesafe.config.ConfigFactory;
import java.util.UUID;

public class Config {
  public final String clientId;
  public final String kafkaBootstrapServers;
  public final String topic;

  private Config(String kafkaBootstrapServers, String topic) {
    this.clientId = UUID.randomUUID().toString();
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.topic = topic;
  }

  public static Config loadConfiguration() {
    final com.typesafe.config.Config loadedConf = ConfigFactory.load().getConfig("app");
    final String kafkaBootstrapServers = loadedConf.getString("kafka-bootstrap-servers");
    final String topic = loadedConf.getString("topic");
    return new Config(kafkaBootstrapServers, topic);
  }
}
