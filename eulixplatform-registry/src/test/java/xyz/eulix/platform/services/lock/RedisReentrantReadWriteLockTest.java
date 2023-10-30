package xyz.eulix.platform.services.lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.redis.client.RedisClient;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author VvV
 * @date 2023/8/23
 */
@QuarkusTest
public class RedisReentrantReadWriteLockTest {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    DistributedReadWriteLockFactory lockFactory;

    @Inject
    RedisClient redisClient;

    /**
     * 测试读锁 加锁和解锁流程
     */
    @Test
    void testReadLock() {
        String keyName = "testKey_read";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        DistributedLock readLock = readWriteLock.readLock();

        boolean isLocked = readLock.tryLock();

        if (isLocked) {
            LOG.infov("acquire read lock success, keyName:{0}", keyName);
            try {
                LOG.infov("do something.");
            } finally {
                readLock.unlock();
                LOG.infov("release read lock success, keyName:{0}", keyName);
            }
        } else {
            LOG.infov("acquire read lock fail, keyName:{0}", keyName);
        }

        Assertions.assertTrue(isLocked);
    }

    /**
     * 测试写锁加锁解锁流程
     */
    @Test
    void testWriteLock() {
        String keyName = "testKey_write";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        DistributedLock writeLock = readWriteLock.writeLock();

        boolean isLocked = writeLock.tryLock();

        if (isLocked) {
            LOG.infov("acquire write lock success, keyName:{0}", keyName);
            try {
                LOG.infov("do something.");
                // Do something
            } finally {
                writeLock.unlock();
                LOG.infov("release write lock success, keyName:{0}", keyName);
            }
        } else {
            LOG.infov("acquire write lock fail, keyName:{0}", keyName);
        }

        Assertions.assertTrue(isLocked);
    }

    /**
     * 测试读锁并发加锁解锁
     */
    @Test
    void testReadLockConcurrent() throws InterruptedException {
        String keyName = "testKey_read_lock_concurrent";

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger success = new AtomicInteger();

        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                boolean isLocked;
                DistributedLock readLock = readWriteLock.readLock();
                try {
                    isLocked = readLock.tryLock(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (isLocked) {
                    LOG.infov("acquire read lock success, keyName:{0}", keyName);
                    try {
                        LOG.infov("do something.");
                        success.getAndIncrement();
                    } finally {
                        readLock.unlock();
                        LOG.infov("release lock success, keyName:{0}", keyName);
                    }
                } else {
                    LOG.infov("acquire read lock fail, keyName:{0}", keyName);
                }

                Assertions.assertTrue(isLocked);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Assertions.assertEquals(10, success.get());
    }

    /**
     * 测试写锁并发加锁解锁
     */
    @Test
    void testWriteLockConcurrent() throws InterruptedException {
        String keyName = "testKey_write_lock_concurrent";

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger success = new AtomicInteger();

        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                boolean isLocked;
                DistributedLock writeLock = readWriteLock.writeLock();
                try {
                    isLocked = writeLock.tryLock(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (isLocked) {
                    LOG.infov("acquire read lock success, keyName:{0}", keyName);
                    try {
                        LOG.infov("do something.");
                        success.getAndIncrement();
                    } finally {
                        writeLock.unlock();
                        LOG.infov("release lock success, keyName:{0}", keyName);
                    }
                } else {
                    LOG.infov("acquire read lock fail, keyName:{0}", keyName);
                }

                Assertions.assertTrue(isLocked);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Assertions.assertEquals(10, success.get());
    }

    /**
     * 测试读读是否共享
     */
    @Test
    void testReadReadSharing() throws JsonProcessingException {
        String keyName = "test_read_sharing";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);
        int readLockCount = 5;
        for (int i = 0; i < readLockCount; i++) {
            boolean isLocked = readWriteLock.readLock().tryLock();
            Assertions.assertTrue(isLocked);
        }

        // 验证读锁总数量
        Map<String, String> map = getHashAsMap(keyName);
        Assertions.assertEquals(6, map.size()); // 5个key+1个mode

    }

    /**
     * 测试读锁重入
     */
    @Test
    void testReadReentrant() {
        String keyName = "test_read_reentrant";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);
        RedisReentrantReadWriteLock.ReadLock readLock = (RedisReentrantReadWriteLock.ReadLock)readWriteLock.readLock();
        String lockValue = readLock.getLockValue();
        Assertions.assertTrue(readLock.tryLock());
        Assertions.assertTrue(readLock.tryLock());
        Map<String, String> map = getHashAsMap(keyName);
        Assertions.assertEquals(2, Integer.valueOf(map.get(lockValue)));
    }

    /**
     * 测试写锁重入
     */
    @Test
    void testWriteReentrant() {
        String keyName = "test_write_reentrant";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);
        RedisReentrantReadWriteLock.WriteLock writeLock = (RedisReentrantReadWriteLock.WriteLock)readWriteLock.writeLock();
        String lockValue = writeLock.getLockValue();
        Assertions.assertTrue(writeLock.tryLock());
        Assertions.assertTrue(writeLock.tryLock());
        Map<String, String> map = getHashAsMap(keyName);
        Assertions.assertEquals(2, Integer.valueOf(map.get(lockValue)));
    }

    /**
     * 工具方法  通过锁keyName获取锁信息
     * @param keyName
     * @return
     */
    private Map<String, String> getHashAsMap(String keyName) {
        // 获取hash的所有字段和值
        Response response = redisClient.hgetall(keyName);

        // 将结果转换为一个Map
        Map<String, String> map = response.getKeys().stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> response.get(key).toString()
                ));

        return map;
    }

    /**
     * 测试读写互斥
     */
    @Test
    void testReadWriteMutex() {
        String keyName = "test_read_write_mutex";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        // Acquire a read lock
        boolean isReadLocked = readWriteLock.readLock().tryLock();
        Assertions.assertTrue(isReadLocked);

        // Attempt to acquire a write lock
        boolean isWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertFalse(isWriteLocked);
    }

    /**
     * 测试写写互斥
     */
    @Test
    void testWriteWriteMutex() throws InterruptedException {
        String keyName = "test_write_write_mutex";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        // Acquire a write lock
        boolean isFirstWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertTrue(isFirstWriteLocked);

        // Attempt to acquire another write lock
        boolean isSecondWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertFalse(isSecondWriteLocked);
    }

    /**
     * 测试多线程并发的读写锁
     */
    @Test
    void testConcurrentReadWriteLock() throws InterruptedException {
        String keyName = "test_concurrent_read_write";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);
        int numThreads = 10;
        int numReaders = 5;
        int numWriters = 5;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        // Create and start reader threads
        for (int i = 0; i < numReaders; i++) {
            Thread readerThread = new Thread(() -> {
                try {
                    startLatch.await();
                    DistributedLock readLock = readWriteLock.readLock();
                    boolean isLocked = readLock.tryLock(10, TimeUnit.SECONDS);
                    if (isLocked) {
                        try {
                            LOG.infov("Acquired read lock successfully, keyName: {0}", keyName);
                            // Perform read operation
                            LOG.infov("Do something.");
                        } finally {
                            readLock.unlock();
                            LOG.infov("Released read lock successfully, keyName: {0}", keyName);
                        }
                    } else {
                        LOG.infov("Failed to acquire read lock, keyName: {0}", keyName);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
            readerThread.start();
        }

        // Create and start writer threads
        for (int i = 0; i < numWriters; i++) {
            Thread writerThread = new Thread(() -> {
                try {
                    startLatch.await();
                    DistributedLock writeLock = readWriteLock.writeLock();
                    boolean isLocked = writeLock.tryLock(10, TimeUnit.SECONDS);
                    if (isLocked) {
                        try {
                            LOG.infov("Acquired write lock successfully, keyName: {0}", keyName);
                            // Perform write operation
                            LOG.infov("Do something.");
                        } finally {
                            writeLock.unlock();
                            LOG.infov("Released write lock successfully, keyName: {0}", keyName);
                        }
                    } else {
                        LOG.infov("Failed to acquire write lock, keyName: {0}", keyName);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
            writerThread.start();
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to finish
        finishLatch.await();

        Map<String, String> map = getHashAsMap(keyName);
        Assertions.assertEquals(0, map.size());
    }

    /**
     * 测试锁降级
     */
    @Test
    void testLockDegradation() {
        String keyName = "testKey_lock_degradation";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.RedisReentrantReadWriteLock);

        DistributedLock writeLock = readWriteLock.writeLock();
        DistributedLock readLock = ((RedisReentrantReadWriteLock.WriteLock) writeLock).getCorrespondingReadLock();

        boolean isLocked = writeLock.tryLock();

        if (isLocked) {
            LOG.infov("acquire write lock success, keyName:{0}", keyName);
            try {
                LOG.infov("do something.");
                // Do something

                // Lock degradation
                readLock.tryLock();
                LOG.infov("downgraded to read lock, keyName:{0}", keyName);
            } finally {
                writeLock.unlock();
                LOG.infov("release write lock success, keyName:{0}", keyName);
            }

            try {
                // Continue doing something with read lock
                LOG.infov("continue doing something with read lock, keyName:{0}", keyName);
            } finally {
                readLock.unlock();
                LOG.infov("release read lock success, keyName:{0}", keyName);
            }
        } else {
            LOG.infov("acquire write lock fail, keyName:{0}", keyName);
        }

        Assertions.assertTrue(isLocked);
    }
}
