package com.zhenik.tapad1.query;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class QueryRedisService {
  private final StatefulRedisConnection<String, String> connection;
  private final RedisCommands<String, String> syncQuery;

  public QueryRedisService(String redisUri) {
    this.connection = getRedisConnection(redisUri);
    this.syncQuery = connection.sync();
  }

  private StatefulRedisConnection<String, String> getRedisConnection(String redisUrl) {
    RedisClient client = RedisClient.create(redisUrl);
    StatefulRedisConnection<String, String> connection = client.connect();
    return connection;
  }

  public String get(String key) {
    return syncQuery.get(key);
  }

  public void closeRedisConnection() {
    connection.close();
  }
}
