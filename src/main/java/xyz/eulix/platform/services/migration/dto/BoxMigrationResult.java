package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.NetworkClient;

import java.util.List;

/**
 *  盒子割接结果
 */
@Data(staticConstructor = "of")
public class BoxMigrationResult {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "盒子的注册码，用于后续平台对于盒子访问合法性的验证")
    private final String boxRegKey;

    @Schema(description = "为盒子分配的 network client 信息")
    private final NetworkClient networkClient;

    @Schema(description = "用户列表")
    private final List<UserMigrationResult> userResults;
}
