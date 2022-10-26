package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.Max;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SubdomainGenInfoV2 {
    @Schema(description = "有效期，单位秒，最长7天")
    @Max(604800)
    private Integer effectiveTime;
}
