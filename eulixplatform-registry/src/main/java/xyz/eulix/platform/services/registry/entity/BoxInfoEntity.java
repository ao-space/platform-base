package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "box_info")
public class BoxInfoEntity extends BaseEntity {
  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @Column(name = "description")
  private String desc;

  // 扩展信息，json结构
  @Column(name = "extra")
  private String extra;

  @Column(name = "box_pub_key")
  private String boxPubKey;

  @Column(name = "auth_type")
  @ValueOfEnum(enumClass = AuthTypeEnum.class, valueMethod = "getName")
  private String authType;

  @OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
  @JoinColumn(name ="box_uuid",referencedColumnName="box_uuid",insertable=false,updatable=false)
  private RegistryBoxEntity registryBoxEntity;

}
