package xyz.eulix.platform.services.lock.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.lock.entity.ReentrantLockEntity;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author VvV
 * @date 2023/7/31
 */
@ApplicationScoped
public class ReentrantLockRepository implements PanacheRepository<ReentrantLockEntity> {

    public ReentrantLockEntity findByLockKey(String lockKey) {
        return this.find("lock_key", lockKey).firstResult();
    }

    public void save(ReentrantLockEntity lock) {
            this.persistAndFlush(lock);
    }

    public void update(ReentrantLockEntity lock) {
        this.update("lock_value = ?1, expires_at = ?2, reentrant_count = ?3 where lock_key = ?4",
                    lock.getLockValue(), lock.getExpiresAt(), lock.getReentrantCount(), lock.getLockKey());
    }

    public void deleteByLockKey(String lockKey) {
        this.delete("lock_key=?1", lockKey);
    }
}
