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

    @Inject
    ReentrantReadWriteLockRepository lockRepository;

    @Inject
    EntityManager entityManager;

    /**
     * 获取读锁
     * 数据库中是否存在锁记录
     *     是：
     *        锁如果过期 更新锁信息（新读锁）通过乐观锁是否更新成功 判断加锁是否成功
     *        如果锁被其他实例持有写锁  加锁失败
     *        增加读锁总重入数量 增加相应实例的读锁重入数量 乐观锁更新
     *     否：加新的读锁
     * @param key
     * @param value
     * @param timeout
     * @return 加锁结果 成功或失败
     */
    @Transactional
    public boolean tryReadLock(String key, String value, Integer timeout) {

        ReentrantReadWriteLockEntity lockEntity = lockRepository.findByLockKey(key);

        // 如果数据库中已经存在该锁
        if (lockEntity != null) {
            ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(lockEntity);

            // 如果锁已经过期了
            if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
                ReentrantReadWriteLockEntity entity = newReadLock(key, value, timeout);
                entity.setVersion(lockEntity.getVersion());
                return updateEntity(entity);
            }

            // 如果当前存在写锁 并且 该写锁被其他实例持有
            if (lock.getWriteLockCount() != 0 && !lock.getWriteLockOwnerUUID().equals(value)) {
                LOG.debugv("acquire read lock fail, keyName:{0}, value:{1}, ttl:{2}", key, value, lock.getExpiresAt());
                return false;
            }

            // 读锁总重入数量+1 并且设置当前实例的重入次数
            int readCount = lock.getReadLockCount();
            lock.setReadLockCount(readCount + 1);
            Map<String, Integer> readHolds = lock.getReadHoldsMap();
            readHolds.put(value, readHolds.getOrDefault(value, 0) + 1);
            lock.setReadHoldsMap(readHolds);

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
        } else {
            // 当前锁不存在 竞争加锁
            boolean isSave = saveEntity(newReadLock(key, value, timeout));
            if (isSave) {
                LOG.debugv("acquire read lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("failed to acquire read lock. The lock is already held by another instance, keyName:{0}", key);
                // 竞争失败
                return false;
            }
        }
    }

    /**
     * 释放读锁
     * 数据库中是否存在锁记录：
     *     否：return
     *     是：检查该实例对应的读锁是否存在  如果不存在 抛出异常
     *         减少读锁总重入数量 减少相应实例的读锁重入数量 乐观锁更新锁信息
     * @param key
     * @param value
     * @param timeout
     */
    @Transactional
    public void releaseReadLock(String key, String value, Integer timeout) {

        ReentrantReadWriteLockEntity lockEntity = lockRepository.findByLockKey(key);

        if (lockEntity == null) {
            LOG.debugv("the lock has been released, keyName:{0}", key);
            return;
        }

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(lockEntity);

        Map<String, Integer> readHolds = lock.getReadHoldsMap();
        // 检查该实例对应的读锁是否存在
        if (!readHolds.containsKey(value)) {
            LOG.warnv("Current thread does not hold read lock, keyName:{0}, lockValue:{1}", key, value);
            throw new RuntimeException("current thread does not hold read lock");
        }

        // 读锁总重入次数-1
        lock.setReadLockCount(lock.getReadLockCount() - 1);

        // 获取当前实例的读锁数量
        int currentCount = readHolds.get(value);

        if (currentCount <= 1) {   // 如果读锁数量小于等于1 释放一次 该实例的读锁全部释放 删除锁记录
            readHolds.remove(value);
            LOG.debugv("release read lock success, keyName:{0}, lockValue:{1}", key, value);
        } else {  // 减少重入次数后 重入次数不为0
            readHolds.put(value, currentCount - 1);
            LOG.debugv("Decrease read lock times success, keyName:{0}, lockValue:{1}", key, value);
        }
        // 将锁记录的相关修改 同步到数据库
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReadHoldsMap(readHolds);
        boolean isUpdate = updateEntity(this.reentrantReadWriteLockToEntity(lock));
        if (isUpdate) {
            LOG.debugv("release lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
        } else {
            // TODO 乐观锁更新失败 是否可以递归重试
            releaseReadLock(key, value, timeout);
        }
    }

    /**
     * 获取写锁
     * 数据库中是否存在锁记录
     *     是：锁如果过期 更新锁信息（新锁）
     *         如果存在写锁  加锁失败
     *         如果存在不是当前实例的写锁  加锁失败
     *     否：尝试加锁
     * @param key
     * @param value
     * @param timeout
     * @return
     */
    @Transactional
    public boolean tryWriteLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity entity = lockRepository.findByLockKey(key);

        if (entity != null) {
            // 锁过期
            if (entity.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
                ReentrantReadWriteLockEntity newEntity = newWriteLock(key, value, timeout);
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

            // 如果存在写锁 加锁失败
            if (entity.getReadLockCount() > 0) {
                LOG.debugv("acquire write lock failed, read locks are held by other instances, keyName:{0}, value:{1}", key, value);
                return false;
            }

            int writeCount = entity.getWriteLockCount();
            // 如果存在写锁 且不是当前实例持有 加锁失败
            if (writeCount > 0 && entity.getWriteLockOwnerUUID() != null && !entity.getWriteLockOwnerUUID().equals(value)) {
                LOG.debugv("acquire write lock failed, write lock is held by other instance, keyName:{0}, value:{1}", key, value);
                return false;
            }
            entity.setWriteLockOwnerUUID(value);
            entity.setWriteLockCount(writeCount + 1);
            entity.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
            boolean isUpdate = updateEntity(entity);
            if (isUpdate) {
                LOG.debugv("acquire write lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {    // 乐观锁更新失败
                LOG.debugv("update write lock failed, keyName:{0}, value:{1}", key, value);
                return false;
            }
        } else {
            boolean isSave = saveEntity(newWriteLock(key, value, timeout));
            if (isSave) {
                LOG.debugv("acquire write lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("failed to acquire write lock. The lock is already held by another instance, keyName:{0}", key);
                // 竞争失败
                return false;
            }
        }
    }

    /**
     * 释放写锁
     * 如果当前数据库中不存在锁记录 return
     * 如果写锁不被当前实例持有 抛出异常
     * 减少锁重入次数 乐观锁更新
     * @param key
     * @param value
     * @param timeout
     */
    @Transactional
    public void releaseWriteLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity entity = lockRepository.findByLockKey(key);

        if (entity == null) {
            LOG.debugv("the lock has been released, keyName:{0}", key);
            return;
        }

        String writeLockOwner = entity.getWriteLockOwnerUUID();
        if (writeLockOwner == null || !writeLockOwner.equals(value)) {
            LOG.warnv("Current thread does not hold write lock, keyName:{0}, lockValue:{1}", key, value);
            throw new RuntimeException("current thread does not hold write lock");
        }

        int writeCount = entity.getWriteLockCount();
        writeCount--;
        if (writeCount <= 0) {
            writeCount = 0;
            entity.setWriteLockOwnerUUID(null);
        }
        entity.setWriteLockCount(writeCount);
        entity.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        // TODO 是否要对更新是否成功进行判断
        updateEntity(entity);
        LOG.debugv("Decrease lock times success, keyName:{0}, lockValue:{1}", key, value);
    }

    @Transactional
    private ReentrantReadWriteLockEntity newReadLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lock.setLockKey(key);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReadLockCount(1);
        lock.setWriteLockCount(0);
        Map<String, Integer> readHolds = new HashMap<>();
        readHolds.put(value, 1);
        lock.setReadHoldsMap(readHolds);

        return this.reentrantReadWriteLockToEntity(lock);
    }

    @Transactional
    public ReentrantReadWriteLockEntity newWriteLock(String key, String value, Integer timeout) {
        ReentrantReadWriteLockEntity entity = new ReentrantReadWriteLockEntity();
        entity.setLockKey(key);
        entity.setReadLockCount(0);
        entity.setWriteLockCount(1);
        entity.setWriteLockOwnerUUID(value);
        entity.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        return entity;
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
            existingEntity.setReadLockCount(entity.getReadLockCount());
            existingEntity.setWriteLockCount(entity.getWriteLockCount());
            existingEntity.setWriteLockOwnerUUID(entity.getWriteLockOwnerUUID());
            existingEntity.setReadHoldsJSON(entity.getReadHoldsJSON());
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
        lock.setReadLockCount(entity.getReadLockCount());
        lock.setWriteLockCount(entity.getWriteLockCount());
        lock.setWriteLockOwnerUUID(entity.getWriteLockOwnerUUID());
        Map<String, Integer> readHolds = new HashMap<>();
        try {
            String readHoldsJson = entity.getReadHoldsJSON();
            if (readHoldsJson != null && !readHoldsJson.isEmpty()) {
                readHolds = new ObjectMapper().readValue(entity.getReadHoldsJSON(), new TypeReference<>() {});
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while parsing readHoldsJSON", e);
        }
        lock.setReadHoldsMap(readHolds);
        lock.setVersion(entity.getVersion());

        return lock;
    }

    private ReentrantReadWriteLockEntity reentrantReadWriteLockToEntity(ReentrantReadWriteLock lock) {
        ReentrantReadWriteLockEntity entity = new ReentrantReadWriteLockEntity();
        entity.setLockKey(lock.getLockKey());
        entity.setExpiresAt(lock.getExpiresAt());
        entity.setReadLockCount(lock.getReadLockCount());
        entity.setWriteLockCount(lock.getWriteLockCount());
        entity.setWriteLockOwnerUUID(lock.getWriteLockOwnerUUID());
        try {
            String readHoldsJSON = new ObjectMapper().writeValueAsString(lock.getReadHoldsMap());
            entity.setReadHoldsJSON(readHoldsJSON);
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
