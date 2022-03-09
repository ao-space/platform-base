package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.ArticleInfo;
import xyz.eulix.platform.services.mgtboard.entity.ArticleEntity;
import xyz.eulix.platform.services.mgtboard.repository.ArticleEntityRepository;
import xyz.eulix.platform.services.mgtboard.repository.CatalogueEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

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

  public List<Long> getArticleIdList(Long rootid){
    List<ArticleEntity> articleEntities=articleEntityRepository.find("cata_id", rootid).list();
    List<Long> articleInfoList = new ArrayList<>();
    articleEntities.forEach(articleEntity -> {articleInfoList.add(articleEntity.getId());});
    return articleInfoList;
  }

  public void deleteArticle(List<Long> articleIds){
    try {
      articleIds.forEach(id -> {articleEntityRepository.deleteById(id);});
    } catch (Exception e) {
      throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
    }
  }

}
