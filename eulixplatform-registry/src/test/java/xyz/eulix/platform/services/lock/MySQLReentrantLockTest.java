package xyz.eulix.platform.services.lock;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.lock.entity.ReentrantLockEntity;
import xyz.eulix.platform.services.lock.repository.ReentrantLockRepository;
import xyz.eulix.platform.services.lock.service.ReentrantLockService;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        lockService.saveEntity(lock);

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
        lockService.saveEntity(lock);

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
        lockService.saveEntity(lock);

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
        lockService.saveEntity(lock);

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

    /**
     * 测试多个线程同时加锁
     * @throws InterruptedException
     */
    @Test
    void testConcurrentTryLock() throws InterruptedException {
        String lockKey = "testKey_concurrent_tryLock";

        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        Map<String, Boolean> results = new ConcurrentHashMap<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                DistributedLock lock = lockFactory.newLock(lockKey, LockType.MySQLReentrantLock);
                boolean result = lock.tryLock();
                results.put(Thread.currentThread().getName(), result);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        int success = 0;
        for (Boolean value : results.values()) {
            if (value) {
                success++;
                LOG.infov("acquire lock success, keyName:{0}, thread:{1}", lockKey, Thread.currentThread());
            }
        }
        Assertions.assertEquals(1, success);

        lockService.deleteLock(lockKey);
    }

    /**
     * 测试多个线程同时加锁解锁
     * @throws InterruptedException
     */
    @Test
    void testConcurrent() throws InterruptedException {
        String keyName = "testKey_concurrent";

        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                DistributedLock lock = lockFactory.newLock(keyName, LockType.MySQLReentrantLock);
                // 加锁
                Boolean isLocked = null;
                try {
                    isLocked = lock.tryLock(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (isLocked) {
                    LOG.infov("acquire lock success, keyName:{0}, thread:{1}", keyName, Thread.currentThread());
                    try {
                        LOG.info("do something.");
                        success.getAndIncrement();
                    } finally {
                        // 释放锁
                        lock.unlock();
                        LOG.infov("release lock success, keyName:{0}, thread:{1}", keyName, Thread.currentThread());
                    }
                } else {
                    LOG.infov("acquire lock fail, keyName:{0}, thread:{1}", keyName, Thread.currentThread());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Assertions.assertEquals(10, success.get());
    }


    /**
     * 测试锁重入以及释放
     */
    @Test
    void testReentrantAndRelease() {
        String keyName = "test_reentrant_and_release";

        DistributedLock lock = lockFactory.newLock(keyName, LockType.MySQLReentrantLock);

        boolean lockAcquired = lock.tryLock();
        Assertions.assertTrue(lockAcquired, "Failed to acquire lock.");

        lockAcquired = lock.tryLock();
        Assertions.assertTrue(lockAcquired, "Failed to acquire lock.");

        // 验证重入次数是否为2
        ReentrantLockEntity entity = lockRepository.findByLockKey(keyName);
        Assertions.assertNotNull(entity, "Lock not found.");
        Assertions.assertEquals(2, entity.getReentrantCount(), "Reentrant count is incorrect.");

        lock.unlock();
        lock.unlock();
        entity = lockRepository.findByLockKey(keyName);
        Assertions.assertNull(entity, "Lock not Released.");
    }
}
