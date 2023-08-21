package xyz.eulix.platform.services.lock;

import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.lock.service.ReentrantReadWriteLockService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author VvV
 * @date 2023/8/4
 */
@ApplicationScoped
public class DistributedReadWriteLockFactory {

    @Inject
    ReentrantReadWriteLockService mysqlLockService;

    @Inject
    ApplicationProperties applicationProperties;

    public DistributedReadWriteLock newLock(String keyName, LockType lockType) {
        Integer timeout = applicationProperties.getLockExpireTime();    // 单位s

        if(lockType.equals(LockType.MySQLReentrantReadWriteLock)) {
            return new MySQLReentrantReadWriteLock(mysqlLockService, keyName, timeout);
        } else {
            throw new IllegalArgumentException("Invalid lock type: " + lockType);
        }

    }
}
