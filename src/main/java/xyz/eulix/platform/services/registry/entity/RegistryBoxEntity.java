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
@Entity @Table(name = "box_registries")
public class RegistryBoxEntity extends BaseEntity {

  @NotBlank
  @Column(name = "box_reg_key")
  private String boxRegKey;

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @NotBlank
  @Column(name = "network_client_id")
  private String networkClientId;

  @Column(name = "network_secret_key")
  private String networkSecretKey;
}
