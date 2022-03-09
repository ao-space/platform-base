package xyz.eulix.platform.services.helpcenter.entity;

import lombok.Data;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "article_info")
public class ArticleEntity extends BaseEntity {
  @Column(name = "title")
  private String title;

  @Column(name = "cata_id")
  private Long cataId;

  @Column(name = "content")
  private String content;

  @Column(name = "state")
  private int state;

  @Column(name = "last_publishd_at")
  private OffsetDateTime publishdAt;
}
