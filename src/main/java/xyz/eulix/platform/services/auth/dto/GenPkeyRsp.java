package xyz.eulix.platform.services.auth.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class GenPkeyRsp {
    // 二维码pkey值
    @Schema(description = "二维码pkey值")
    private final String pkey;

    // 过期时间
    @Schema(description = "pkey有效时间")
    private final OffsetDateTime expiresAt;
}
