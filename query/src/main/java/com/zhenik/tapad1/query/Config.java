package com.zhenik.tapad1.query;

import com.typesafe.config.ConfigFactory;

public class Config {
  public final String host;
  public final int port;
  public final String redisUri;

  private Config(String host, int port, String redisUri) {
    this.host = host;
    this.port = port;
    this.redisUri = redisUri;
  }

  public static Config loadConfiguration() {
    final com.typesafe.config.Config loadedConf = ConfigFactory.load().getConfig("app");
    final String host = loadedConf.getString("host");
    final int port = loadedConf.getInt("port");
    final String redisUri = loadedConf.getString("redis.uri");
    return new Config(host, port, redisUri);
  }
}
