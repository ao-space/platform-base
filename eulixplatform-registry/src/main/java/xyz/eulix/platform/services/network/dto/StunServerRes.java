package xyz.eulix.platform.services.network.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class StunServerRes {
    @Schema(description = "STUN服务器地址")
    private final String stunAddress;
}
