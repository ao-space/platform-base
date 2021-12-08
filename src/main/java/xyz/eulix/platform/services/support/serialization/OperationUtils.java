package xyz.eulix.platform.services.support.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OperationUtils {

  @Inject
  ObjectMapper objectMapper;

  @SneakyThrows
  public String objectToJson(Object object) {
    return objectMapper.writeValueAsString(object);
  }

  @SneakyThrows
  public <T> T jsonToObject(String json, Class<T> clz) {
    return objectMapper.readValue(json, clz);
  }
}
