package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.BoxInfoEntity;
import xyz.eulix.platform.common.support.CommonUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class BoxInfoEntityRepository implements PanacheRepository<BoxInfoEntity> {
    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据box_uuids查询资源
    private static final String FIND_BY_BOXUUIDS = "box_uuid in (?1)";

    public Optional<BoxInfoEntity> findByBoxUUID(String boxUUID) {
        return this.find(FIND_BY_BOXUUID, boxUUID).firstResultOptional();
    }

    public long deleteByBoxUUIDS(List<String> boxUUIDs) {
        return this.delete(FIND_BY_BOXUUIDS, boxUUIDs);
    }

    public List<BoxInfoEntity> findByBoxUUIDS(List<String> boxUUIDs) {
        return this.find(FIND_BY_BOXUUIDS, boxUUIDs).list();
    }

    @Transactional
    public int updateByBoxUUID(String extra, String authType, String boxPubKey, String boxUUID){
        return update("set extra=?1, auth_type=?2, box_pub_key=?3, updated_at=now() where box_uuid=?4 ", extra, authType, boxPubKey, boxUUID);
    }

    @Transactional
    public int updateAuthTypeByBoxUUID(String authType, String boxUUID){
        return update("set auth_type=?1 where box_uuid=?2", authType, boxUUID);
    }

    @Transactional
    public void createBoxInfo(BoxInfoEntity boxInfoEntity) {
        this.persist(boxInfoEntity);
    }

    public PanacheQuery<BoxInfoEntity> findWithBoxRegistries(Boolean isregistry){
        if(CommonUtils.isNull(isregistry)){
            return find("select a from BoxInfoEntity a left join fetch a.registryBoxEntity");
        }else if(Boolean.TRUE.equals(isregistry)){
            return find("select a from BoxInfoEntity a inner join fetch a.registryBoxEntity");
        } else{
            return find("select a from BoxInfoEntity a left join fetch a.registryBoxEntity c where c.boxUUID=null");
        }
    }

    public Long getWithBoxRegistriesCount(Boolean isregistry){
        if(CommonUtils.isNull(isregistry)){
            return this.count();
        }else if(Boolean.TRUE.equals(isregistry)){
            return this.count("from BoxInfoEntity a inner join a.registryBoxEntity");
        } else{
            return this.count("from BoxInfoEntity a left join a.registryBoxEntity c where c.boxUUID=null");
        }
    }
}
