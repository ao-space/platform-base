package xyz.eulix.platform.services.mgtboard.entity;

import lombok.Data;
import xyz.eulix.platform.services.mgtboard.dto.ArticleStateEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "article_info")
public class ArticleEntity extends BaseEntity {
    @NotBlank
    @Column(name = "title")
    private String title;

    @Column(name = "cata_id")
    private Long cataId;

    @Column(name = "content")
    private String content;

    @Column(name = "state")
    @ValueOfEnum(enumClass = ArticleStateEnum.class, valueMethod = "getState")
    private Integer state;

    @Column(name = "last_publishd_at")
    private OffsetDateTime publishdAt;
}
