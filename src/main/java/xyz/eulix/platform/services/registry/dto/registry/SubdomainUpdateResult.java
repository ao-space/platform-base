package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data
public class SubdomainUpdateResult {
  @Schema(description = "是否成功")
  private Boolean success;

  @Schema(description = "盒子的 UUID, success为true时返回")
  private String boxUUID;

  @Schema(description = "用户id, success为true时返回")
  private String userId;

  @Schema(description = "全局唯一的 subdomain, success为true时返回")
  private String subdomain;

  @Schema(description = "错误码, success为false时返回")
  private Integer code;

  @Schema(description = "错误消息, success为false时返回")
  private String error;

  @Schema(description = "推荐的subdomain, success为false时返回")
  private List<String> recommends;
}
