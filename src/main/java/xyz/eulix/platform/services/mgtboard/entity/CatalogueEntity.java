package xyz.eulix.platform.services.mgtboard.entity;

import lombok.Data;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Data
@Entity
@Table(name = "catalogue_info")
public class CatalogueEntity extends BaseEntity {
    @NotNull
    @Column(name = "cata_name")
    private String cataName;

    @Column(name = "parent_id")
    private Long parentId;
}

