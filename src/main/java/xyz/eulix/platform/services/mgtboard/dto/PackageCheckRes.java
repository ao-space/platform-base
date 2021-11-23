package xyz.eulix.platform.services.mgtboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
@NoArgsConstructor
public class PackageCheckRes {
    // 是否存在更新
    @Schema(description = "是否存在更新")
    private Boolean newVersionExist = false;

    // app是否需要关联更新
    @Schema(description = "app是否需要关联更新")
    private Boolean isAppNeedUpdate;

    // box是否需要关联更新
    @Schema(description = "box是否需要关联更新")
    private Boolean isBoxNeedUpdate;

    // 最新app软件包信息
    private PackageRes latestAppPkg;

    // 最新box软件包信息
    private PackageRes latestBoxPkg;
}
