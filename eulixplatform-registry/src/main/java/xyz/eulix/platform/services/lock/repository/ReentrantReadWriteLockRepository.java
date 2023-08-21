package xyz.eulix.platform.services.lock.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.lock.entity.ReentrantReadWriteLockEntity;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author VvV
 * @date 2023/8/7
 */
@Dependent
public class ReentrantReadWriteLockRepository implements PanacheRepository<ReentrantReadWriteLockEntity> {


    @Inject
    EntityManager entityManager;

    public ReentrantReadWriteLockEntity findByLockKey(String lockKey) {
        entityManager.clear();
        return this.find("lock_key", lockKey).firstResult();
    }

    public void save(ReentrantReadWriteLockEntity lock) {
        this.persistAndFlush(lock);
    }

    public void deleteByLockKey(String lockKey) {
        this.delete("lock_key", lockKey);
    }
}
