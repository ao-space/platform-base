package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.ArticleEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ArticleEntityRepository  implements PanacheRepository<ArticleEntity> {
    private static final String FIND_BY_IDS = "id in (?1)";

    @Transactional
    public int update(Long id, String content){
        return update("set content=?1 where id=?2", content, id);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {this.delete(FIND_BY_IDS, ids);}
}
