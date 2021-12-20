package xyz.eulix.platform.services.network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor(staticName = "of")
public class NetworkAuthReq {
    @NotBlank
    @Schema(description = "network client id")
    private String clientId;

    @NotBlank
    @Schema(description = "network client 访问密钥")
    private String secretKey;
}