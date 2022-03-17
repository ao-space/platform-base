package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Data
public class FileReq {
	@NotNull
	@Schema(description = "file路径")
	private String filePath;
}
