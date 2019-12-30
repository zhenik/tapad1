package com.zhenik.tapad1.processor;

import com.typesafe.config.ConfigFactory;
import java.util.UUID;

public class Config {
  public final String clientId;
  public final String kafkaBootstrapServers;
  public final String inputTopic;
  public final String outputTopic;

  private Config(String kafkaBootstrapServers, String inputTopic, String outputTopic) {
    this.clientId = UUID.randomUUID().toString();
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
  }

  public static Config loadConfiguration() {
    final com.typesafe.config.Config loadedConf = ConfigFactory.load().getConfig("app");
    final String kafkaBootstrapServers = loadedConf.getString("kafka-bootstrap-servers");
    final String inputTopic = loadedConf.getString("input-topic");
    final String outputTopic = loadedConf.getString("output-topic");
    return new Config(kafkaBootstrapServers, inputTopic, outputTopic);
  }
}
