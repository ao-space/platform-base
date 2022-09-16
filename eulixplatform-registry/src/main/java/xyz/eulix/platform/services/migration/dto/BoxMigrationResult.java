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
    private String boxUUID;

    @Schema(description = "为盒子分配的 network client 信息")
    private NetworkClient networkClient;

    @Schema(description = "用户列表")
    private List<UserMigrationResult> userResults;
}
