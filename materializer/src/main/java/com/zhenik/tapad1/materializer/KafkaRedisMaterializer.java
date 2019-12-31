package com.zhenik.tapad1.materializer;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaRedisMaterializer {
  private static final Logger log = LoggerFactory.getLogger(KafkaRedisMaterializer.class);
  private final KafkaStreams streams;
  private final StatefulRedisConnection<String, String> connection;

  public KafkaRedisMaterializer(Config config) {
    final Properties streamsConfiguration = getStreamsConfiguration(config);
    this.connection = getRedisConnection(config.redisUrl);
    this.streams =
        new KafkaStreams(
            buildTopology(config.sourceTopic, connection.async()),
            streamsConfiguration);
  }

  private Properties getStreamsConfiguration(Config config) {
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "materializer-processor-app-id");
    streamsConfiguration.put(
        StreamsConfig.CLIENT_ID_CONFIG, "materializer-processor-" + config.clientId);
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.setProperty(StreamsConfig.TOPOLOGY_OPTIMIZATION, StreamsConfig.OPTIMIZE);
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
    return streamsConfiguration;
  }

  Topology buildTopology(
      String sourceTopic, RedisStringAsyncCommands<String, String> asyncConnection) {

    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    final KStream<String, String> sourceStream = streamsBuilder.stream(sourceTopic);
    sourceStream.peek((key, value) -> log.debug("{}:{}", key, value)).foreach(asyncConnection::set);
    final Topology topology = streamsBuilder.build();
    log.info("Topology: \n{}", topology.describe());
    return topology;
  }

  private StatefulRedisConnection<String, String> getRedisConnection(String redisUrl) {
    RedisClient client = RedisClient.create(redisUrl);
    StatefulRedisConnection<String, String> connection = client.connect();
    return connection;
  }

  public void start() {
    //streams.cleanUp(); // don't do this in prod as it clears your state stores
    final CountDownLatch startLatch = new CountDownLatch(1);
    streams.setStateListener(
        (newState, oldState) -> {
          if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
            startLatch.countDown();
          }
        });
    streams.start();
    try {
      if (!startLatch.await(60, TimeUnit.SECONDS)) {
        throw new RuntimeException("Streams never finished rebalancing on startup");
      }
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    log.info("Started Service analytics app");
  }

  public void stop() {
    if (streams != null) {
      streams.close();
      connection.close();
    }
  }
}
