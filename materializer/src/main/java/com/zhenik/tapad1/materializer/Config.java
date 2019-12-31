package com.zhenik.tapad1.materializer;

import com.typesafe.config.ConfigFactory;
import java.util.UUID;

public class Config {
  public final String clientId;
  public final String kafkaBootstrapServers;
  public final String sourceTopic;
  public final String redisUri;

  private Config(String kafkaBootstrapServers, String sourceTopic, String redisUri) {
    this.clientId = UUID.randomUUID().toString();
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.sourceTopic = sourceTopic;
    this.redisUri = redisUri;
  }

  public static Config loadConfiguration() {
    final com.typesafe.config.Config loadedConf = ConfigFactory.load().getConfig("app");
    final String kafkaBootstrapServers = loadedConf.getString("kafka.bootstrap-servers");
    final String sourceTopic = loadedConf.getString("kafka.source-topic");
    final String redisUri = loadedConf.getString("redis.uri");
    return new Config(kafkaBootstrapServers, sourceTopic, redisUri);
  }
}
