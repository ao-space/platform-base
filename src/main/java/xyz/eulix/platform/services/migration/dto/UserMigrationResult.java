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
    private final String userId;

    @Schema(description = "用户的注册码，用于后续平台对于用户访问合法性的验证")
    private final String userRegKey;

    @Schema(description = "用户被指定的子域名字段")
    private final String subdomain;

    @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
    private final String userType;

    @Schema(description = "Client列表")
    private final List<ClientMigrationResult> clientResults;
}
