package com.zhenik.tapad1.schema.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class AnalyticsSerializer implements Serializer<Analytics> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override public byte[] serialize(String topic, Analytics analytics) {
    if (Objects.isNull(analytics)) {
      return null;
    }
    try {
      return objectMapper.writeValueAsBytes(analytics);
    } catch (Exception e) {
      throw new SerializationException("Error serializing message", e);
    }
  }
}
