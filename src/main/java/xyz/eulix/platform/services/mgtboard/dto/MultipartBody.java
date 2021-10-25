package xyz.eulix.platform.services.mgtboard.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class MultipartBody {
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream file;

    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    @Size(max = 500)
    @Schema(required = true, maxLength = 500 ,description = "文件名称")
    public String fileName;
}
