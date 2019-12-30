package com.zhenik.tapad1.processor.messaging;

import com.zhenik.tapad1.processor.Config;
import com.zhenik.tapad1.schema.Schema;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsKafkaProcessor {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaProcessor.class);
  private final KafkaStreams streams;
  private final String topic;

  public AnalyticsKafkaProcessor(Config config) {
    this.topic = config.topic;
    final Properties streamsConfiguration = getStreamsConfiguration(config);
    this.streams = new KafkaStreams(buildTopology(new StreamsBuilder()), streamsConfiguration);
  }

  private Properties getStreamsConfiguration(Config config) {
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "analytics-processor-app-id");
    streamsConfiguration.put(
        StreamsConfig.CLIENT_ID_CONFIG, "analytics-processor-" + config.clientId);
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
    // Specify default (de)serializers for record keys and for record values.
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    // Records should be flushed every 10 seconds. This is less than the default
    // in order to keep this example interactive.
    // streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
    // For illustrative purposes we disable record caches.
    // streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    // Use a temporary directory for storing state, which will be automatically removed after the
    // test.
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/opt/stream-analytics");
    return streamsConfiguration;
  }

  Topology buildTopology(StreamsBuilder streamsBuilder) {
    final KStream<String, String> sourceStream = streamsBuilder.stream(topic);
    sourceStream.peek((key, value) -> System.out.println(key + ": " + value));

    final KStream<String, String> userStream =
        sourceStream
            .mapValues(value -> value.split(" ")[0])
            .peek((key, value) -> System.out.println("Impressions " + key + ": " + value));

    userStream
        .groupByKey()
        .aggregate(
            (Initializer<HashSet<String>>) HashSet::new,
            (k, v, a) -> {
              a.add(v);
              return a;
            })
        .toStream()
        .peek((k, v) -> System.out.println("Users " + k + ": " + v));

    final KStream<String, String>[] actionStreams =
        sourceStream
            .mapValues(value -> value.split(" ")[1])
            .branch(
                (key, value) -> value.equalsIgnoreCase(Schema.CLICK),
                (key, value) -> value.equalsIgnoreCase(Schema.IMPRESSION));

    actionStreams[0]
        .groupByKey()
        .count()
        .toStream()
        .peek((key, value) -> System.out.println("Clicks " + key + ": " + value))
        .to("click-stream-v1", Produced.with(Serdes.String(), Serdes.Long()));

    actionStreams[1]
        .groupByKey()
        .count()
        .toStream()
        .peek((key, value) -> System.out.println("Impressions " + key + ": " + value))
        .to("impression-stream-v1", Produced.with(Serdes.String(), Serdes.Long()));

    final Topology topology = streamsBuilder.build();
    log.info("Topology: \n{}",topology.describe());
    return topology;
  }

  public void start() {
    streams.cleanUp(); // don't do this in prod as it clears your state stores
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
    }
  }

}
