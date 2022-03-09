package xyz.eulix.platform.services.helpcenter.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.helpcenter.dto.ArticleInfo;
import xyz.eulix.platform.services.helpcenter.entity.ArticleEntity;
import xyz.eulix.platform.services.helpcenter.repository.ArticleEntityRepository;
import xyz.eulix.platform.services.helpcenter.repository.CatalogueEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticleService {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  CatalogueEntityRepository catalogueEntityRespository;

  @Inject
  ArticleEntityRepository articleEntityRepository;

  public ArticleInfo createNewArticle(Long rootid, String title, String context){
    var articleEntity = new ArticleEntity();
    articleEntity.setContent(context);
    articleEntity.setTitle(title);
    articleEntity.setState(0);
    articleEntityRepository.persist(articleEntity);
    return ArticleInfo.of(articleEntity.getId(), articleEntity.getTitle(),articleEntity.getCataId(), articleEntity.getState(),
            null, articleEntity.getCreatedAt(), articleEntity.getUpdatedAt());
  }

  public ArticleInfo updateArticle(Long rootid, Integer id, String context){
    var articleEntity = new ArticleEntity();
    articleEntity.setContent(context);
    articleEntity.setState(0);
    articleEntityRepository.persist(articleEntity);
    return ArticleInfo.of(articleEntity.getId(), articleEntity.getTitle(),articleEntity.getCataId(), articleEntity.getState(),
            null, articleEntity.getCreatedAt(), articleEntity.getUpdatedAt());
  }

  public List<ArticleInfo> getArticleList(Long rootid){
    List<ArticleEntity> articleEntities=articleEntityRepository.find("cata_id", rootid).list();
    List<ArticleInfo> articleInfoList = new ArrayList<>();
    articleEntities.forEach(articleEntity -> articleInfoList.add(ArticleInfo.of(articleEntity.getId(), articleEntity.getTitle(),
            articleEntity.getCataId(), articleEntity.getState(),
            articleEntity.getPublishdAt(), articleEntity.getCreatedAt(), articleEntity.getUpdatedAt())));
    return articleInfoList;
  }
}
