package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.CatalogueEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CatalogueEntityRepository implements PanacheRepository<CatalogueEntity> {
    private static final String FIND_BY_IDS = "id in (?1)";

    @Transactional
    public void deleteByNodeIds(List<Long> ids) {this.delete(FIND_BY_IDS, ids);}

    @Transactional
    public Long deleteByNodeId(Long id){
        return delete("id in (?1)", id);
    }

    @Transactional
    public int update(Long id, String name){
        return update("set cata_name=?1, updated_at=now() where id=?2", name, id);
    }

    public List<CatalogueEntity> findByCataName(String name) {
        return find("cata_name", name).list();
    }

    @Transactional
    public void create(CatalogueEntity catalogueEntity){
        persist(catalogueEntity);
    }
}
