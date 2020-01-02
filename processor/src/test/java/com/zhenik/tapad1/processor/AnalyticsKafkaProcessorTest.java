package com.zhenik.tapad1.processor;

import java.util.Arrays;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnalyticsKafkaProcessorTest {
  private final String topicIn = "topic-in";
  private final String topicOut = "topic-out";
  private TopologyTestDriver testDriver;


  @Before public void start() { }

  @After public void tearDown() {
    testDriver.close();
    testDriver = null;
  }

  @Test
  public void checkAnalyticsAggregation() {
    final Topology topology = AnalyticsKafkaProcessor.buildTopology(topicIn, topicOut);
    testDriver = new TopologyTestDriver(topology, Utils.getProperties("clientId", "kafkabootstrapServers"));

    final ConsumerRecordFactory<String, String> factory =
        new ConsumerRecordFactory<>(topicIn, new StringSerializer(), new StringSerializer());
    final ConsumerRecord<byte[], byte[]> record1 = factory.create(topicIn, "123", "zhenik click");
    final ConsumerRecord<byte[], byte[]> record2 = factory.create(topicIn, "123", "zhenik click");
    final ConsumerRecord<byte[], byte[]> record3 = factory.create(topicIn, "123", "jeqo impression");
    final ConsumerRecord<byte[], byte[]> record4 = factory.create(topicIn, "1234", "cat impression");
    final ConsumerRecord<byte[], byte[]> record5 = factory.create(topicIn, "1234", "dog click");

    testDriver.pipeInput(Arrays.asList(record1, record2, record3, record4, record5));
    final ProducerRecord<String, String> outRecord1 = testDriver.readOutput(topicOut, new StringDeserializer(), new StringDeserializer());
    final ProducerRecord<String, String> outRecord2 = testDriver.readOutput(topicOut, new StringDeserializer(), new StringDeserializer());
    final ProducerRecord<String, String> outRecord3 = testDriver.readOutput(topicOut, new StringDeserializer(), new StringDeserializer());
    final ProducerRecord<String, String> outRecord4 = testDriver.readOutput(topicOut, new StringDeserializer(), new StringDeserializer());
    final ProducerRecord<String, String> outRecord5 = testDriver.readOutput(topicOut, new StringDeserializer(), new StringDeserializer());

    assertEquals("123", outRecord1.key());
    assertEquals("123", outRecord2.key());
    assertEquals("123", outRecord3.key());
    assertEquals("1234", outRecord4.key());
    assertEquals("1234", outRecord5.key());

    assertEquals("unique_users,1\nclicks,1\nimpressions,0", outRecord1.value());
    assertEquals("unique_users,1\nclicks,2\nimpressions,0", outRecord2.value());
    assertEquals("unique_users,2\nclicks,2\nimpressions,1", outRecord3.value());
    assertEquals("unique_users,1\nclicks,0\nimpressions,1", outRecord4.value());
    assertEquals("unique_users,2\nclicks,1\nimpressions,1", outRecord5.value());
  }
}