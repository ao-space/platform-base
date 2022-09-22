package xyz.eulix.platform.services.token.dto;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServiceEnum {
  PLATFORM("10001", "官方空间平台"),
  PRODUCT("10002","官方产品服务平台"),
  ;

  @Getter
  final String serviceId;
  final String serviceName;

  public static ServiceEnum fromValue(String value) {
    return Arrays.stream(values()).filter(serviceType -> serviceType.getServiceId().equals(value)).findFirst().orElseThrow();
  }


}
