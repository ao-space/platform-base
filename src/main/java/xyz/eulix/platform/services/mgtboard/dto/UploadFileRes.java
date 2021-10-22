package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Data(staticConstructor = "of")
public class UploadFileRes {
    @Schema(description = "文件id")
    private final String fileId;

    @Schema(description = "原始文件名称")
    private final String origin;

    @Schema(description = "文件大小，单位字节")
    private final Long fileSize;

    @Schema(description = "文件地址")
    private final String fileUrl;
}