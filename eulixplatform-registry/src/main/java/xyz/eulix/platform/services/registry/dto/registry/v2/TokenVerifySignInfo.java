package xyz.eulix.platform.services.registry.dto.registry.v2;

import java.util.List;
import lombok.Data;

@Data(staticConstructor = "of")
public class TokenVerifySignInfo {
  private final String boxUUID;
  private final List<String> serviceIds;
}
