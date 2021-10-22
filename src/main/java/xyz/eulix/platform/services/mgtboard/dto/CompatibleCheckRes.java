package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class CompatibleCheckRes {
    // app是否需要强制更新
    @Schema(description = "app是否需要强制更新")
    private Boolean isAppForceUpdate = false;

    // box是否需要强制更新
    @Schema(description = "box是否需要强制更新")
    private Boolean isBoxForceUpdate = false;

    // 最新app软件包信息
    @Schema(description = "最新app软件包信息")
    private final AppPkgReq lastestAppPkg;

    // 最新box软件包信息
    @Schema(description = "最新box软件包信息")
    private final BoxPkgRes lastestBoxPkg;
}
