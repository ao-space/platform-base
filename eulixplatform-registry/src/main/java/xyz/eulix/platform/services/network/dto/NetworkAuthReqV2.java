package xyz.eulix.platform.services.network.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor(staticName = "of")
public class NetworkAuthReqV2 {
  @NotBlank
  @Schema(description = "network client id")
  private String networkClientId;

  @NotBlank
  @Schema(description = "network client 访问密钥")
  private String networkSecretKey;
}