package xyz.eulix.platform.services.registry.dto.registry.v2;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data
public class SubdomainUpdateInfoV2 {
    @Schema(description = "子域名，最长100字符")
    @Size(max = 100)
    @NotBlank
    private String subdomain;
}
