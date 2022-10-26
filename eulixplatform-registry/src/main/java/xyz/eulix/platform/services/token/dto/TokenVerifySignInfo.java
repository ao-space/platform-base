package xyz.eulix.platform.services.token.dto;

import java.util.List;
import lombok.Data;

@Data(staticConstructor = "of")
public class TokenVerifySignInfo {
  private final String boxUUID;
  private final List<String> serviceIds;
}
