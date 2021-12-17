package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "box_info")
public class BoxInfoEntity extends BaseEntity {
  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @Column(name = "`desc`")
  private String desc;

  // 扩展信息，json结构
  @Column(name = "extra")
  private String extra;
}
