package xyz.eulix.platform.services.lock.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.lock.entity.ReentrantLockEntity;
import xyz.eulix.platform.services.lock.repository.ReentrantLockRepository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;

/**
 * @author VvV
 * @date 2023/7/31
 */
@Dependent
public class ReentrantLockService {

    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ReentrantLockRepository lockRepository;

    /**
     * 先去数据库中查询锁是否存在
     *     是：lockValue是否为当前实例
     *         是：重入锁 count+1
     *         否：加锁失败  返回过期时间(记录日志)
     *     否：加锁
     * @param key     锁唯一标识
     * @param value   实例唯一标识
     * @param timeout 锁过期时间
     * @return 是否加锁成功
     */
    @Transactional
    public boolean tryLock(String key, String value, Integer timeout) {

        ReentrantLockEntity lock = lockRepository.findByLockKey(key);

        if (lock != null) {
            if (lock.getLockValue().equals(value)) {
                // lockValue是当前实例，重入锁 count+1  重置过期时间
                lock.setReentrantCount(lock.getReentrantCount() + 1);
                lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
                lockRepository.update(lock);
                LOG.debugv("acquire lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                // lockValue不是当前实例，加锁失败，打印过期时间
                LOG.debugv("acquire lock fail, keyName:{0}, value:{1}, ttl:{2}", key, value, lock.getExpiresAt());
                return false;
            }
        }

        // 当前锁 不存在 加锁 设置锁具体信息
        lock = new ReentrantLockEntity();
        lock.setLockKey(key);
        lock.setLockValue(value);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReentrantCount(1);

        // 多个实例争夺加锁 利用唯一索引 确保只有一个实例加锁成功
        if (saveEntity(lock)) {
            LOG.debugv("acquire lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
            return true;
        } else {
            LOG.debugv("acquire lock failed, the lock is already held by another instance, keyName:{0}, value:{1}", key, value);
            return false;
        }
    }

    /**
     * 通过key是否能查询到锁记录
     *     否：锁不存在 返回
     *     是：判断锁是否过期
     *         是：删除锁
     *         否：锁是否被当前实例持有
     *             否：抛出异常
     *             是：锁重入次数-1 判断重入次数是否为0
     *                 是：删除锁
     *                 否：重置锁过期时间
     *
     * @param key     锁唯一标识
     * @param value   实例唯一标识
     * @param timeout 锁过期时间
     */
    @Transactional
    public void releaseLock(String key, String value, Integer timeout) {
        ReentrantLockEntity lock = lockRepository.findByLockKey(key);

        if (lock == null) {
            LOG.debugv("lock not exits, keyName:{0}", key);
            return;
        }

        // 判断锁的过期时间
        if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            // 如果锁过期，删除锁
            lockRepository.delete(lock);
            LOG.debugv("lock has expired, keyName:{0}", key);
        } else {
            // 如果没有过期，通过对比value和lock.getLockValue()判断当前实例是否持有锁
            if (!value.equals(lock.getLockValue())) {
                // 如果当前实例不持有锁，抛出异常
                LOG.warnv("Current thread does not hold lock, keyName:{0}, lockValue:{1}", key, value);
                throw new RuntimeException("current thread does not hold lock");
            } else {
                // 如果当前实例持有锁，锁重入次数-1
                lock.setReentrantCount(lock.getReentrantCount() - 1);

                if (lock.getReentrantCount() == 0) {
                    // 如果锁重入次数为0，删除锁
                    lockRepository.delete(lock);
                    LOG.debugv("release lock success, keyName:{0}, lockValue:{1}", key, value);
                } else {
                    // 如果锁未完全释放，重置过期时间
                    lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
                    lockRepository.update(lock);
                    LOG.debugv("Decrease lock times success, keyName:{0}, lockValue:{1}", key, value);
                }
            }
        }
    }

    /**
     * 检查锁是否过期 过期则删除锁
     * @param key
     */
    @Transactional
    public void deleteExpiredLock(String key) {
        ReentrantLockEntity lock = lockRepository.findByLockKey(key);
        if (lock != null && lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            // 如果过期，删除锁
            lockRepository.deleteByLockKey(key);
        }
    }

    /**
     * 为了测试使用
     * @param lockKey
     */
    @Transactional
    public void deleteLock(String lockKey){
        lockRepository.deleteByLockKey(lockKey);
    }

    @Transactional
    public boolean saveEntity(ReentrantLockEntity entity) {
        try {
            lockRepository.save(entity);
            return true;
        } catch (Exception e) {
            LOG.debugv("insert entity failed, keyName:{0}, message:{1}", entity.getLockKey(), e.getMessage());
            return false;
        }
    }
}
