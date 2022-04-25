package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class ReservedDomainCreateRsp {
    @Schema(description = "条目id")
    private final Long regexId;
}
