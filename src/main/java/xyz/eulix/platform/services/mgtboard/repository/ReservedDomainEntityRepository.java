package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.ReservedDomainEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

// 保留域名存储
@ApplicationScoped
public class ReservedDomainEntityRepository  implements PanacheRepository<ReservedDomainEntity> {

    // 查询 id 列表
    private static final String FIND_BY_REGEX_IDS = "id in (?1)";

    // 根据正则表达式的主键id来删除.
    public long deleteByRegexIds(List<Long> regexIds) {
        return this.delete(FIND_BY_REGEX_IDS, regexIds);
    }

    // 根据正则表达式的主键id来更新.
    public long updateByRegexId(Long regexId, ReservedDomainEntity entity) {
        return this.update("set regex=?1, description=?2, updated_at=now() where id=?3",
                entity.getRegex(), entity.getDescription(), regexId);
    }
}
