package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "client_registries")
public class RegistryClientEntity extends BaseEntity {
  @NotBlank
  @Column(name = "client_reg_key")
  private String clientRegKey;

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @NotBlank
  @Column(name = "user_id")
  private String userId;

  @NotBlank
  @Column(name = "client_uuid")
  private String clientUUID;

  @NotBlank
  @Column(name = "type")
  @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName")
  private String registryType;
}
