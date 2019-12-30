package com.zhenik.tapad1.schema.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class AnalyticsDeserializer implements Deserializer<Analytics> {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {}

  @Override
  public Analytics deserialize(String topic, byte[] bytes) {
    if (Objects.isNull(bytes)) {
      return null;
    }

    Analytics analytics;
    try {
      analytics = objectMapper.treeToValue(objectMapper.readTree(bytes), Analytics.class);
    } catch (Exception e) {
      throw new SerializationException(e);
    }

    return analytics;
  }

  @Override
  public void close() {}
}
