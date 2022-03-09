package xyz.eulix.platform.services.helpcenter.entity;

import lombok.Data;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.*;


@Data
@Entity
@Table(name = "catalogue_info")
public class CatalogueEntity extends BaseEntity {
  @Column(name = "cata_name")
  private String cataName;

  @Column(name = "parent_id")
  private Long parentId;
}

