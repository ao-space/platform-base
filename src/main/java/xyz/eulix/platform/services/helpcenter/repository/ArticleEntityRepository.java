package xyz.eulix.platform.services.helpcenter.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.helpcenter.entity.ArticleEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class ArticleEntityRepository  implements PanacheRepository<ArticleEntity> {
  @Transactional
  public int update(Long id, String content){
    return update("set content=?1 where id=?2", content, id);
  }
}
