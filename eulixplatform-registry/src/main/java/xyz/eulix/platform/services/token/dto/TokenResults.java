package xyz.eulix.platform.services.token.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class TokenResults {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "盒子的注册码")
    private final List<TokenResult> tokenResults;

}
