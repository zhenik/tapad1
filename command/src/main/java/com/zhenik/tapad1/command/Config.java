package com.zhenik.tapad1.command;

import com.typesafe.config.ConfigFactory;
import java.util.UUID;

public class Config {
  public final String clientId;
  public final String kafkaBootstrapServers;
  public final String topic;
  public final String host;
  public final int port;

  private Config(String kafkaBootstrapServers, String topic, String host, int port) {
    this.clientId = UUID.randomUUID().toString();
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.topic = topic;
    this.host = host;
    this.port = port;
  }

  public static Config loadConfiguration() {
    final com.typesafe.config.Config loadedConf = ConfigFactory.load().getConfig("app");
    final String kafkaBootstrapServers = loadedConf.getString("kafka-bootstrap-servers");
    final String topic = loadedConf.getString("topic");
    final String host = loadedConf.getString("host");
    final int port = loadedConf.getInt("port");
    return new Config(kafkaBootstrapServers, topic, host, port);
  }
}
