package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 用户割接信息
 */
@Data
public class UserMigrationInfo {
    @NotBlank
    @Schema(description = "用户的 ID")
    private String userId;

    @NotBlank
    @Schema(description = "用户被指定的子域名字段")
    private String subdomain;

    @NotBlank
    @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
    @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
    private String userType;

    @Valid
    @Schema(description = "Client 列表")
    private List<ClientMigrationInfo> clientInfos;
}
