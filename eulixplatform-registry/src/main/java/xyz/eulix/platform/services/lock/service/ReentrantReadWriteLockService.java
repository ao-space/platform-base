package xyz.eulix.platform.services.lock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.lock.dto.ReentrantReadWriteLock;
import xyz.eulix.platform.services.lock.entity.ReentrantReadWriteLockEntity;
import xyz.eulix.platform.services.lock.repository.ReentrantReadWriteLockRepository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VvV
 * @date 2023/8/6
 */
@Dependent
public class ReentrantReadWriteLockService {

    private static final Logger LOG = Logger.getLogger("app.log");

    private static final String READ_MODE = "read";
    private static final String Write_MODE = "write";

    @Inject
    ReentrantReadWriteLockRepository lockRepository;

    @Inject
    EntityManager entityManager;

    /**
     * 通过key获取锁记录
     *     如果锁不存在  加读锁 (insert)
     *     如果锁过期   加读锁 (乐观锁更新锁信息)
     *     如果 mode=read 或者 (mode=write&&写锁是当前实例的)
     *         增加读锁记录 (对于第二种情况 写锁解锁时 需要特别处理 写锁释放完 若还存在读锁则将锁模式设置为"read")
     *     否则 加锁失败 返回锁剩余过期时间
     * @param key      锁唯一标识
     * @param value    实例(客户端)唯一uuid
     * @param timeout  过期时间
     * @return 加锁是否成功
     */
    @Transactional
    public boolean tryReadLock(String key, String value, Integer timeout) {

        ReentrantReadWriteLockEntity lockEntity = lockRepository.findByLockKey(key);

        if (lockEntity == null) {
            // 当前锁不存在 多个实例操作数据库 竞争加锁 只有一个实例能保存数据成功
            boolean isSave = saveEntity(newLock(key, value, timeout, READ_MODE));
            if (isSave) {
                LOG.debugv("acquire read lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("failed to acquire read lock, keyName:{0}", key);
                return false;
            }
        }

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(lockEntity);
        // 如果锁已经过期了
        if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            ReentrantReadWriteLockEntity entity = newLock(key, value, timeout, READ_MODE);
            entity.setVersion(lockEntity.getVersion());
            boolean isUpdate = updateEntity(entity);
            if (isUpdate) {
                LOG.debugv("acquire read lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {    // 乐观锁更新失败
                LOG.debugv("update read lock failed, keyName:{0}, value:{1}", key, value);
                return false;
            }
        }
        String mode = lock.getMode();
        Map<String, Integer> lockHolds = lock.getLockHoldsMap();
        String writeValue = value + ":" + Write_MODE;
        if (mode.equals(READ_MODE) || (mode.equals(Write_MODE) && lockHolds.containsKey(writeValue))) {
            lockHolds.put(value, lockHolds.getOrDefault(value, 0) + 1);
            // 更新锁过期时间
            lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
            boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("acquire read lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {    // 乐观锁更新失败
                LOG.debugv("update read lock failed, keyName:{0}, value:{1}", key, value);
                return false;
            }
        }

        LOG.debugv("acquire read lock fail, keyName:{0}, lockValue:{1}, expires_at:{2}", key, value, lock.getExpiresAt());
        return false;
   }

    /**
     * 通过key获取锁记录
     *     如果锁不存在 解锁成功
     *     如果锁过期 解锁成功
     * 检查当前实例是否持有锁
     *     如果当前实例不持有锁  抛出异常
     * 当前实例持有锁 减少锁重入次数
     *     如果重入次数变为0  从lockHolds中移除当前实例锁记录
     *     如果删除后 还有其他锁存在 重置过期时间  乐观锁更新  如果失败则重试
     *     如果删除后 没有其他锁存在 删除锁记录(将锁设置过期 代表删除)  乐观锁更新  如果失败则重试
     * @param key      锁唯一标识
     * @param value    实例(客户端)唯一uuid
     * @param timeout  过期时间
     */
    @Transactional
    public void releaseReadLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity lockEntity = lockRepository.findByLockKey(key);

        if (lockEntity == null) {
            LOG.debugv("the lock has been released, keyName:{0}", key);
            return;
        }

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(lockEntity);

        // 如果锁已经过期了
        if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            LOG.debugv("the lock has been expired, keyName:{0}", key);
            return;
        }

        Map<String, Integer> lockHolds = lock.getLockHoldsMap();
        // 如果该实例对应的读锁不存在
        if (!lockHolds.containsKey(value)) {
            LOG.warnv("Current thread does not hold the read lock, keyName:{0}, lockValue:{1}", key, value);
            throw new RuntimeException("current thread does not hold read lock");
        }

        // 减少当前实例的读锁重入次数
        int reentrantCount = lockHolds.get(value) - 1;
        if (reentrantCount == 0) {
            lockHolds.remove(value);
        } else {
            lockHolds.put(value, reentrantCount);
        }
        // 如果还有锁 重置锁过期时间
        if (lockHolds.size() > 0) {
            lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
            boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("Decrease read lock times success, keyName:{0}, lockValue:{1}", key, value);
            } else {    // 乐观锁更新失败
                LOG.debugv("update read lock failed, retry, keyName:{0}, value:{1}", key, value);
                releaseReadLock(key, value, timeout);
            }
        } else {
            // 将锁设置过期 代表删除
            lock.setExpiresAt(new Timestamp(System.currentTimeMillis() - timeout));
            boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("release read lock success, keyName:{0}, lockValue:{1}", key, value);
            } else {    // 乐观锁更新失败
                LOG.debugv("update read lock failed, retry, keyName:{0}, value:{1}", key, value);
                releaseReadLock(key, value, timeout);
            }
        }
    }

    /**
     * 通过key获取锁记录
     *     如果锁不存在  加写锁(insert)
     *     如果锁过期  加写锁(乐观锁更新)
     *     如果mode=write 检查当前线程是否持有锁
     *         是 增加重入次数、重置过期时间 乐观锁更新锁记录
     *         否 加锁失败
     * @param key      锁唯一标识
     * @param value    实例(客户端)唯一uuid+"write"
     * @param timeout  过期时间
     * @return 加锁是否成功
     */
    @Transactional
    public boolean tryWriteLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity entity = lockRepository.findByLockKey(key);

        if (entity == null) {
            boolean isSave = saveEntity(newLock(key, value, timeout, Write_MODE));
            if (isSave) {
                LOG.debugv("acquire write lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("failed to acquire write lock. The lock is already held by another instance, keyName:{0}", key);
                // 竞争失败
                return false;
            }
        }

        // 如果锁过期
        if (entity.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            ReentrantReadWriteLockEntity newEntity = newLock(key, value, timeout, Write_MODE);
            newEntity.setVersion(entity.getVersion());
            boolean isUpdate = updateEntity(newEntity);
            if (isUpdate) {
                LOG.debugv("acquire write lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {    // 乐观锁更新失败
                LOG.debugv("acquire write lock failed, keyName:{0}, value:{1}", key, value);
                return false;
            }
        }

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(entity);
        Map<String, Integer> lockHolds = lock.getLockHoldsMap();
        if (lock.getMode().equals(Write_MODE) && lockHolds.containsKey(value)) {
            lockHolds.put(value, lockHolds.get(value) + 1);
            lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
            boolean isUpdate = updateEntity(reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("acquire write lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {    // 乐观锁更新失败
                LOG.debugv("acquire write lock failed, keyName:{0}, value:{1}", key, value);
                return false;
            }
        }

        LOG.debugv("acquire write lock failed, keyName:{0}, value:{1}", key, value);
        return false;
    }

    /**
     * 通过key获取锁记录
     *     如果锁不存在  解锁成功
     *     如果锁已经过期  解锁成功
     *     如果mode=read 或者当前实例不持有写锁 抛出异常
     *     减少写锁重入次数
     *         重入次数是否大于0
     *             是 设置锁过期时间
     *             否 删除当前实例的写锁 判断是否map中key的数量
     *                 key数量为1(该key为mode) 当前锁不被任何实例持有 删除整个锁记录
     *                 key数量不为1 将mode设置为read (当前实例还持有读锁)
     * @param key      锁唯一标识
     * @param value    实例(客户端)唯一uuid+"write"
     * @param timeout  过期时间
     */
    @Transactional
    public void releaseWriteLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity entity = lockRepository.findByLockKey(key);

        if (entity == null) {
            LOG.debugv("the lock has been released, keyName:{0}", key);
            return;
        }

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(entity);

        // 如果锁已经过期了
        if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            LOG.debugv("the lock has been expired, keyName:{0}", key);
            return;
        }

        Map<String, Integer> lockHolds = lock.getLockHoldsMap();
        if (lock.getMode().equals(READ_MODE) || !lockHolds.containsKey(value)) {
            LOG.warnv("Current thread does not hold the write lock, keyName:{0}, lockValue:{1}", key, value);
            throw new RuntimeException("current thread does not hold read lock");
        }

        int reentrantCount = lockHolds.get(value) - 1;

        if (reentrantCount > 0) {
            lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
            lockHolds.put(value, reentrantCount);
            boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("Decrease write lock times success, keyName:{0}, lockValue:{1}", key, value);
            } else {    // 乐观锁更新失败
                LOG.debugv("update write lock failed, retry, keyName:{0}, value:{1}", key, value);
                releaseWriteLock(key, value, timeout);
            }
        } else {
            lockHolds.remove(value);
            if (lockHolds.size() == 0) {
                // 将过期时间设置为过去  代表删除
                lock.setExpiresAt(new Timestamp(System.currentTimeMillis() - timeout));
            } else {
                // 如果将当前实例的写锁移除 lockHolds里还有锁 则是当前实例的读锁
                lock.setMode(READ_MODE);
            }
            boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
            if (isUpdate) {
                LOG.debugv("release write lock success, keyName:{0}, lockValue:{1}", key, value);
            } else {    // 乐观锁更新失败
                LOG.debugv("update write lock failed, retry, keyName:{0}, value:{1}", key, value);
                releaseWriteLock(key, value, timeout);
            }
        }
    }

    @Transactional
    private ReentrantReadWriteLockEntity newLock(String key, String value, Integer timeout, String mode) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lock.setLockKey(key);
        lock.setMode(mode);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        Map<String, Integer> lockHolds = new HashMap<>();
        lockHolds.put(value, 1);
        lock.setLockHoldsMap(lockHolds);
        return this.reentrantReadWriteLockToEntity(lock);
    }

    @Transactional
    public boolean saveEntity(ReentrantReadWriteLockEntity entity) {
        try {
            lockRepository.save(entity);
            return true;
        } catch (Exception e) {
            LOG.debugv("insert entity failed, keyName:{0}, message:{1}", entity.getLockKey(), e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean updateEntity(ReentrantReadWriteLockEntity entity) {
        ReentrantReadWriteLockEntity existingEntity = lockRepository.findByLockKey(entity.getLockKey());
        if (existingEntity != null) {
            existingEntity.setExpiresAt(entity.getExpiresAt());
            existingEntity.setMode(entity.getMode());
            existingEntity.setLockHoldsJSON(entity.getLockHoldsJSON());
            try {
                entityManager.flush();
                return true;
            } catch (OptimisticLockException e) {
                LOG.debugv("Optimistic Lock Exception while updating entity with lock key: {0} and version: {1}. Exception details: {2}",
                        entity.getLockKey(), entity.getVersion(), e.getMessage());
                return false;
            }
        } else {
            LOG.debugv("No entity found with lock key: {0}", entity.getLockKey());
            return false;
        }
    }



    private ReentrantReadWriteLock entityToReentrantReadWriteLock(ReentrantReadWriteLockEntity entity) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lock.setLockKey(entity.getLockKey());
        lock.setExpiresAt(entity.getExpiresAt());
        lock.setMode(entity.getMode());
        Map<String, Integer> readHolds = new HashMap<>();
        try {
            String readHoldsJson = entity.getLockHoldsJSON();
            if (readHoldsJson != null && !readHoldsJson.isEmpty()) {
                readHolds = new ObjectMapper().readValue(entity.getLockHoldsJSON(), new TypeReference<>() {});
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while parsing readHoldsJSON", e);
        }
        lock.setLockHoldsMap(readHolds);
        lock.setVersion(entity.getVersion());

        return lock;
    }

    private ReentrantReadWriteLockEntity reentrantReadWriteLockToEntity(ReentrantReadWriteLock lock) {
        ReentrantReadWriteLockEntity entity = new ReentrantReadWriteLockEntity();
        entity.setLockKey(lock.getLockKey());
        entity.setExpiresAt(lock.getExpiresAt());
        entity.setMode(lock.getMode());
        try {
            String readHoldsJSON = new ObjectMapper().writeValueAsString(lock.getLockHoldsMap());
            entity.setLockHoldsJSON(readHoldsJSON);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting readHoldsMap to JSON", e);
        }
        entity.setVersion(lock.getVersion());
        return entity;
    }

    @Transactional
    public void deleteEntity(String lockKey) {
        lockRepository.deleteByLockKey(lockKey);
    }

}
