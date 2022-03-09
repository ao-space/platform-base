package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class CatalogueReq {
    @NotBlank
    @Schema(required = true, description = "目录名字")
    private String cataName;

    @NotBlank
    @Schema(required = true, description = "父目录id")
    private String parentId;
}
