package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * 用户割接结果
 */
@Data(staticConstructor = "of")
public class UserMigrationResult {
    @Schema(description = "用户的 ID")
    private String userId;

    @Schema(description = "用户被指定的子域名字段")
    private String subdomain;

    @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
    private String userType;

    @Schema(description = "Client列表")
    private List<ClientMigrationResult> clientResults;
}
