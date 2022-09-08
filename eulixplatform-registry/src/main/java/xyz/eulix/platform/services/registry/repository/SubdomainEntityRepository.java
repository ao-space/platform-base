package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;


/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class SubdomainEntityRepository implements PanacheRepository<SubdomainEntity> {
    // 根据user_domain查询资源
    private static final String FIND_BY_USER_DOMAIN = "user_domain=?1";

    // 根据subdomain查询资源
    private static final String FIND_BY_SUBDOMAIN = "subdomain=?1";

    // 根据subdomain更新资源
    private static final String UPDATE_BY_SUBDOMAIN = "user_id=?1, state=?2, updated_at=now(), expires_at=null where subdomain=?3";

    // 根据box_uuid、user_id查询资源
    private static final String FIND_BY_BOXUUID_USERID = "box_uuid=?1 AND user_id=?2";

    // 根据box_uuid查询资源
    private static final String FIND_BY_BOXUUID = "box_uuid=?1";

    // 根据box_uuid、user_id更新资源
    private static final String UPDATE_BY_BOXUUID_USERID = "subdomain=?1, user_domain=?2, updated_at=now() where box_uuid=?3 AND user_id=?4";

    public Optional<SubdomainEntity> findByUserDomain(String userDomain) {
        return this.find(FIND_BY_USER_DOMAIN, userDomain).firstResultOptional();
    }

    public Optional<SubdomainEntity> findBySubdomain(String subdomain) {
        return this.find(FIND_BY_SUBDOMAIN, subdomain).firstResultOptional();
    }

    public void updateBySubdomain(String userId, Integer state, String subdomain) {
        this.update(UPDATE_BY_SUBDOMAIN, userId, state, subdomain);
    }

    public void deleteSubdomainByUserId(String boxUUID, String userId) {
        this.delete(FIND_BY_BOXUUID_USERID, boxUUID, userId);
    }

    public void deleteSubdomainByBoxUUID(String boxUUID) {
        this.delete(FIND_BY_BOXUUID, boxUUID);
    }

    public SubdomainEntity findByBoxUUIDAndUserId(String boxUUID, String userId){
        return this.find(FIND_BY_BOXUUID_USERID, boxUUID, userId).firstResult();
    }

    // 用正则来匹配subdomain
    public List<SubdomainEntity> findByRegularExpression(String regex){
        // 需要使用 mysql 特有的 regexp/rlike 关键字来进行正则查询.
        return getEntityManager().createNamedQuery("SubdomainEntity.findByRegexp").setParameter("regexp", regex).getResultList();
    }

    public void updateSubdomainByBoxUUIDAndUserId(String boxUUID, String userId, String subdomain, String userDomain) {
        this.update(UPDATE_BY_BOXUUID_USERID, subdomain, userDomain, boxUUID, userId);
    }
}
