package com.zhenik.tapad1.schema;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

public class TopicCreate {

  public static void main(String[] args) {
    final String sourceTopic = "timestamp-user-action-v1";
    createTopic(sourceTopic, 10);
  }

  public static void createTopic(final String topicName, final int partitions) {
    final short replicationFactor = 1;

    // Create admin client
    try (final AdminClient adminClient = KafkaAdminClient.create(buildDefaultClientConfig())) {
      try {
        // Define topic
        final NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

        // Create topic, which is async call.
        final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));

        // Since the call is Async, Lets wait for it to complete.
        createTopicsResult.values().get(topicName).get();
      } catch (InterruptedException | ExecutionException e) {
        if (!(e.getCause() instanceof TopicExistsException)) {
          throw new RuntimeException(e.getMessage(), e);
        }
        // TopicExistsException - Swallow this exception, just means the topic already exists.
      }
    }
  }

  private static Properties buildDefaultClientConfig() {
    Properties defaultClientConfig = new Properties();
    defaultClientConfig.put("bootstrap.servers", "localhost:19092");
    defaultClientConfig.put("client.id", "admin-client-id-1");
    return defaultClientConfig;
  }
}
