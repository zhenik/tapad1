package com.zhenik.tapad1.processor.messaging;

import com.zhenik.tapad1.processor.Config;
import com.zhenik.tapad1.schema.serde.Analytics;
import com.zhenik.tapad1.schema.serde.AnalyticsDeserializer;
import com.zhenik.tapad1.schema.serde.AnalyticsSerializer;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zhenik.tapad1.schema.Schema.CLICK;
import static com.zhenik.tapad1.schema.Schema.IMPRESSION;

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
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/streams-analytics");
    return streamsConfiguration;
  }

  Topology buildTopology(StreamsBuilder streamsBuilder) {
    Serde<Analytics> analyticsSerde =
        Serdes.serdeFrom(new AnalyticsSerializer(), new AnalyticsDeserializer());
    analyticsSerde.configure(new HashMap<>(), false);

    final KStream<String, String> sourceStream = streamsBuilder.stream(topic);
    sourceStream
        .peek((key, value) -> System.out.println(key + ": " + value))
        .groupByKey()
        .aggregate(
            Analytics::new,
            (k, v, a) -> {
              final String[] userAndAction = v.split(" ");
              a.addUser(userAndAction[0]);
              if (IMPRESSION.equalsIgnoreCase(userAndAction[1])){
                a.incrementImpressions();
              } else if (CLICK.equalsIgnoreCase(userAndAction[1])) {
                a.incrementClicks();
              }
              return a;
            }, Materialized.with(Serdes.String(), analyticsSerde))
        .toStream()
        .peek((k, v) -> System.out.println("Analytics " + k + ": " + v.toString()));

    final Topology topology = streamsBuilder.build();
    log.info("Topology: \n{}",topology.describe());
    return topology;
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
    }
  }

}
