package xyz.eulix.platform.services.basic.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * 空间平台APIs
 */
@Data(staticConstructor = "of")
public class PlatformApiResults {
    @Schema(description = "空间平台API列表")
    private final List<PlatformApi> platformApis;
}
