package xyz.eulix.platform.services.lock;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.lock.entity.ReentrantLockEntity;
import xyz.eulix.platform.services.lock.repository.ReentrantLockRepository;
import xyz.eulix.platform.services.lock.service.ReentrantLockService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author VvV
 * @date 2023/7/27
 */
@QuarkusTest
public class MySQLReentrantLockTest {

    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    DistributedLockFactory lockFactory;

    @Inject
    ReentrantLockRepository lockRepository;

    @Inject
    ReentrantLockService lockService;

    @Test
    void testLock() {
        String keyName = "distributedLockKey1";
        DistributedLock lock = lockFactory.newLock(keyName, LockType.MySQLReentrantLock);
        // 加锁
        Boolean isLocked = lock.tryLock();
        if (isLocked) {
            LOG.infov("acquire lock success, keyName:{0}", keyName);
            try {
                // 这里写需要处理业务的业务代码
                LOG.info("do something.");
            } finally {
                // 释放锁
                lock.unlock();
                LOG.infov("release lock success, keyName:{0}", keyName);
            }
        } else {
            LOG.infov("acquire lock fail, keyName:{0}", keyName);
        }
        Assertions.assertTrue(isLocked);
    }

    /**
     * 测试存在过期锁是否可以加锁成功
     * 先创建一个锁
     * 等待锁过期后  尝试加锁
     */
    @Test
    void testExitExpiredLock() throws InterruptedException {
        String lockKey = "testKey_expired";
        String lockValue = UUID.randomUUID().toString();

        // 确保测试锁不存在 不会发生插入冲突
        lockService.deleteLock(lockKey);

        // 创建一个过期的锁
        ReentrantLockEntity lock = new ReentrantLockEntity();
        lock.setLockKey(lockKey);
        lock.setLockValue(lockValue);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() - 1000)); // 设置过去的时间作为过期时间
        lock.setReentrantCount(0);
        lockService.saveLock(lock);

        // 等待一段时间，确保锁已经过期
        Thread.sleep(2000);

        // 尝试加锁
        DistributedLock lock2 = lockFactory.newLock(lockKey, LockType.MySQLReentrantLock);
        boolean isLocked = lock2.tryLock();
        Assertions.assertTrue(isLocked, "Failed to acquire lock on expired lock.");

        // 删除测试数据
        lockService.deleteLock(lockKey);
    }

    /**
     * 测试重入锁情况
     * 重复加锁  验证重入次数是否符合预期
     */
    @Test
    void testReentrant() {
        String lockKey = "testKey_reentrant";
        String lockValue = UUID.randomUUID().toString();
        int timeout = 30 * 1000; // 30s

        // 确保测试锁不存在 不会发生插入冲突
        lockService.deleteLock(lockKey);

        // 第一次加锁
        boolean lockAcquired = lockService.tryLock(lockKey, lockValue, timeout);
        Assertions.assertTrue(lockAcquired, "Failed to acquire lock.");

        // 第二次加锁
        lockAcquired = lockService.tryLock(lockKey, lockValue, timeout);
        Assertions.assertTrue(lockAcquired, "Failed to acquire reentrant lock.");

        // 验证重入次数是否为2
        ReentrantLockEntity lock = lockRepository.findByLockKey(lockKey);
        Assertions.assertNotNull(lock, "Lock not found.");
        Assertions.assertEquals(2, lock.getReentrantCount(), "Reentrant count is incorrect.");

        // 删除测试数据
        lockService.deleteLock(lockKey);

    }

    /**
     * 测试当前锁已经存在其他线程持有 当前线程是否会加锁失败
     */
    @Test
    void testConcurrent() throws InterruptedException {
        String lockKey = "testKey_concurrent";
        String thread1UUID = UUID.randomUUID().toString();
        String thread2UUID = UUID.randomUUID().toString();
        int timeout = 30 * 1000; // 30s

        // 确保测试锁不存在 不会发生插入冲突
        lockService.deleteLock(lockKey);

        // 创建一个锁并保存
        ReentrantLockEntity lock = new ReentrantLockEntity();
        lock.setLockKey(lockKey);
        lock.setLockValue(thread1UUID);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReentrantCount(1);
        lockService.saveLock(lock);

        // 在另一个线程中尝试加锁
        Thread otherThread = new Thread(() -> {
            boolean lockAcquired = lockService.tryLock(lockKey, thread2UUID, timeout);
            Assertions.assertFalse(lockAcquired, "Acquired lock when it should have failed due to concurrent lock.");
        });
        otherThread.start();
        // 确保线程顺序正确执行
        otherThread.join();

        // 删除测试数据
        lockService.deleteLock(lockKey);
    }

    /**
     * 测试释放过期锁
     */
    @Test
    void testReleaseExpiredLock() {
        String lockKey = "testKey_release_expired_lock";
        String lockValue = UUID.randomUUID().toString();
        int timeout = 30 * 1000; // 30s

        // 确保测试锁不存在 不会发生插入冲突
        lockService.deleteLock(lockKey);

        // 创建一个过期的锁
        ReentrantLockEntity lock = new ReentrantLockEntity();
        lock.setLockKey(lockKey);
        lock.setLockValue(lockValue);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() - 1000)); // 设置过去的时间作为过期时间
        lock.setReentrantCount(0);
        lockService.saveLock(lock);

        // 释放过期锁
        lockService.releaseLock(lockKey, lockValue, timeout);

        // 验证锁是否被成功删除
        lock = lockRepository.findByLockKey(lockKey);
        Assertions.assertNull(lock, "Failed to release expired lock.");
    }

    /**
     * 测试释放被其他线程持有的锁
     */
    @Test
    void testReleaseOtherThreadLock() {
        String lockKey = "testKey_release_other_thread_lock";
        String otherLockValue = UUID.randomUUID().toString();
        String thisLockValue = UUID.randomUUID().toString();
        int timeout = 30 * 1000; // 30s

        // 创建一个锁并加锁
        ReentrantLockEntity lock = new ReentrantLockEntity();
        lock.setLockKey(lockKey);
        lock.setLockValue(otherLockValue);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReentrantCount(1);
        lockService.saveLock(lock);

        // 尝试释放被其他线程持有的锁
        Assertions.assertThrows(RuntimeException.class, () -> lockService.releaseLock(lockKey, thisLockValue, timeout));

        // 验证锁是否仍然存在
        lock = lockRepository.findByLockKey(lockKey);
        Assertions.assertNotNull(lock, "Lock should not have been released by other thread.");

        // 删除测试数据
        lockService.deleteLock(lockKey);
    }

    /**
     * 测试释放重入锁
     * 1.重入次数不为0  是否重置过期时间
     * 2.重入次数为0 是否删除锁
     */
    @Test
    void testReleaseReentrantLock() {
        String lockKey = "testKey_release_reentrant_lock";
        String lockValue = UUID.randomUUID().toString();
        int timeout = 30 * 1000; // 30s

        // 创建一个重入次数为2的锁并保存
        ReentrantLockEntity lock = new ReentrantLockEntity();
        lock.setLockKey(lockKey);
        lock.setLockValue(lockValue);
        lock.setExpiresAt(new Timestamp(System.currentTimeMillis() + timeout));
        lock.setReentrantCount(2);
        lockService.saveLock(lock);

        // 释放重入锁
        lockService.releaseLock(lockKey, lockValue, timeout);

        // 验证锁的重入次数是否减少
        lock = lockRepository.findByLockKey(lockKey);
        Assertions.assertNotNull(lock, "Lock not found.");
        Assertions.assertEquals(1, lock.getReentrantCount(), "Reentrant count is incorrect.");

        // 释放重入锁
        lockService.releaseLock(lockKey, lockValue, timeout);

        // 验证锁是否被成功删除
        lock = lockRepository.findByLockKey(lockKey);
        Assertions.assertNull(lock, "Failed to release reentrant lock.");
    }
}
