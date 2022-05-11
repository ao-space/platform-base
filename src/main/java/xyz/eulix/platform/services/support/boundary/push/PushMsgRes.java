package xyz.eulix.platform.services.support.boundary.push;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
public class PushMsgRes {
    @Schema(description = "是否成功")
    private String ret;

    @Schema(description = "详细信息")
    private PushMsgData data;
}
