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

        if (lockEntity == null) {
            // 当前锁不存在 竞争加锁
            boolean isSave = saveEntity(newLock(key, value, timeout, READ_MODE));
            if (isSave) {
                LOG.debugv("acquire read lock success, keyName:{0}, value:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("failed to acquire read lock. The lock is already held by another instance, keyName:{0}", key);
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

        ReentrantReadWriteLock lock = entityToReentrantReadWriteLock(entity);

        // 如果锁已经过期了
        if (lock.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            LOG.debugv("the lock has been expired, keyName:{0}", key);
            return;
        }

        if (lock.getMode().equals(Write_MODE)) {
            Map<String, Integer> lockHolds = lock.getLockHoldsMap();
            if (!lockHolds.containsKey(value)) {
                LOG.warnv("Current thread does not hold the write lock, keyName:{0}, lockValue:{1}", key, value);
                throw new RuntimeException("current thread does not hold read lock");
            } else {
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
