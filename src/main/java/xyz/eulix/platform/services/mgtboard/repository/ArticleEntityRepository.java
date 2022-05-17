package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import xyz.eulix.platform.services.mgtboard.dto.ArticleStateEnum;
import xyz.eulix.platform.services.mgtboard.entity.ArticleEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class ArticleEntityRepository  implements PanacheRepository<ArticleEntity> {
    private static final String FIND_BY_IDS = "id in (?1)";

    private static final String FIND_BY_CATA_ID = "cata_id in ?1";

    @Transactional
    public void deleteByIds(List<Long> ids) {this.delete(FIND_BY_IDS, ids);}

    @Transactional
    public void create(ArticleEntity articleEntity){
        if(articleEntity.getState().equals(ArticleStateEnum.PUBLISHED.getState())) {
            articleEntity.setPublishdAt(OffsetDateTime.now());
        }
        persist(articleEntity);
    }

    @Transactional
    public int update(ArticleEntity articleEntity){
        articleEntity.setUpdatedAt(OffsetDateTime.now());
        if(articleEntity.getState().equals(ArticleStateEnum.PUBLISHED.getState())){
            articleEntity.setPublishdAt(OffsetDateTime.now());
            return update("set title=?1, cata_id=?2, content=?3, state=?4, updated_at = now() , last_publishd_at = now() where id=?5",
                    articleEntity.getTitle(),articleEntity.getCataId(), articleEntity.getContent() ,articleEntity.getState(), articleEntity.getId());
        }else{
            return update("set title=?1, cata_id=?2, content=?3, state=?4, updated_at = now() where id=?5",
                    articleEntity.getTitle(),articleEntity.getCataId(), articleEntity.getContent() ,articleEntity.getState(), articleEntity.getId());
        }
    }

    public List<ArticleEntity> findByCataIdAndSortBySortKey(Sort sortKey, Long id, Integer currentPage, Integer pageSize){
        return find(FIND_BY_CATA_ID,sortKey, id).page(currentPage, pageSize).list();
    }

    public List<ArticleEntity> findByTitle(String title) {return find("title", title).list();}

    public  List<ArticleEntity> findByTitleAndCataId(String title, Long cataid){
        return find("title = ?1 and cata_id =?2", title, cataid).list();
    }
    @Transactional
    public void deleteByCataId(Long id){
        delete(FIND_BY_CATA_ID, id);
    }
}
