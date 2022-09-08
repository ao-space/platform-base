package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.common.support.model.BaseEntity;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "user_registries")
public class RegistryUserEntity extends BaseEntity {
  @NotBlank
  @Column(name = "user_reg_key")
  private String userRegKey;

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @NotBlank
  @Column(name = "user_id")
  private String userId;

  @NotBlank
  @Column(name = "type")
  @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
  private String registryType;
}
