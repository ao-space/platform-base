package xyz.eulix.platform.services.registry.dto.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
@JsonIgnoreProperties
public class BoxInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "盒子的描述信息")
    private String desc;

    @Schema(description = "盒子的扩展信息,json格式")
    private Object extra;

    @Schema(description = "盒子是否已注册")
    private boolean isRegistered;

    @Schema(description = "操作时间")
    private OffsetDateTime updatedAt;
}
