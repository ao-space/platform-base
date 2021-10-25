package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data(staticConstructor = "of")
public class DownloadFileReq {
    @NotBlank
    @Schema(required = true, description = "文件地址")
    private String fileUrl;
}