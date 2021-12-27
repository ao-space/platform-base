package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class BoxInfosReq {
  @Valid
  @NotNull
  @Size(max = 1000)
  @Schema(description = "盒子 UUID 列表")
  private List<BoxInfo> boxInfos;
}
