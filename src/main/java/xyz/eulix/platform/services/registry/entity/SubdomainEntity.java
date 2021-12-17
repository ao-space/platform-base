package xyz.eulix.platform.services.registry.entity;

import lombok.*;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainStateEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "subdomain")
public class SubdomainEntity extends BaseEntity {

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @Column(name = "user_id")
  private String userId;

  @NotBlank
  @Column(name = "subdomain")
  private String subdomain;

  @Column(name = "user_domain")
  private String userDomain;

  @NotNull
  @Column(name = "state")
  @ValueOfEnum(enumClass = SubdomainStateEnum.class, valueMethod = "getState")
  private Integer state;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;
}
