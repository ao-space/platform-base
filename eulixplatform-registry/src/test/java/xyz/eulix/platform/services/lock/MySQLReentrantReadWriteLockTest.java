package xyz.eulix.platform.services.lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.lock.entity.ReentrantReadWriteLockEntity;
import xyz.eulix.platform.services.lock.repository.ReentrantReadWriteLockRepository;
import xyz.eulix.platform.services.lock.service.ReentrantReadWriteLockService;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VvV
 * @date 2023/8/11
 */
@QuarkusTest
public class MySQLReentrantReadWriteLockTest {

    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    DistributedReadWriteLockFactory lockFactory;

    @Inject
    ReentrantReadWriteLockRepository lockRepository;

    @Inject
    ReentrantReadWriteLockService lockService;

    /**
     * 测试读锁 加锁和解锁流程
     */
    @Test
    void testReadLock() {
        String keyName = "testKey_read";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

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

        lockService.deleteEntity(keyName);
    }

    /**
     * 测试写锁加锁解锁流程
     */
    @Test
    void testWriteLock() {
        String keyName = "testKey_write";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

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

        lockService.deleteEntity(keyName);
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

        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

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

        lockService.deleteEntity(keyName);
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

        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

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

        lockService.deleteEntity(keyName);
    }

    /**
     * 测试读读是否共享以及读锁可重入
     */
    @Test
    void testReadReadSharing() throws JsonProcessingException {
        String keyName = "test_read_sharing";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);
        int readLockCount = 5;
        for (int i = 0; i < readLockCount; i++) {
            boolean isLocked = readWriteLock.readLock().tryLock();
            Assertions.assertTrue(isLocked);
        }

        ReentrantReadWriteLockEntity entity1 = lockRepository.findByLockKey(keyName);
        // 验证读锁总重入数量
        Assertions.assertEquals(5, entity1.getReadLockCount());
        Map<String, Integer> readHolds = new ObjectMapper().readValue(entity1.getReadHoldsJSON(), new TypeReference<>() {});
        // 验证读锁个数
        Assertions.assertEquals(5, readHolds.size());

        // 验证读锁重入
        MySQLReentrantReadWriteLock.ReadLock readLock = (MySQLReentrantReadWriteLock.ReadLock)readWriteLock.readLock();
        String lockValue = readLock.getLockValue();
        Assertions.assertTrue(readLock.tryLock());
        Assertions.assertTrue(readLock.tryLock());

        ReentrantReadWriteLockEntity entity2 = lockRepository.findByLockKey(keyName);
        readHolds = new ObjectMapper().readValue(entity2.getReadHoldsJSON(), new TypeReference<>() {});

        Assertions.assertEquals(2, readHolds.get(lockValue));

        lockService.deleteEntity(keyName);
    }

    /**
     * 测试读写互斥
     */
    @Test
    void testReadWriteMutex() {
        String keyName = "test_read_write_mutex";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

        // Acquire a read lock
        boolean isReadLocked = readWriteLock.readLock().tryLock();
        Assertions.assertTrue(isReadLocked);

        // Attempt to acquire a write lock
        boolean isWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertFalse(isWriteLocked);

        lockService.deleteEntity(keyName);
    }


    /**
     * 测试写写互斥
     */
    @Test
    void testWriteWriteMutex() {
        String keyName = "test_write_write_mutex";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);

        // Acquire a write lock
        boolean isFirstWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertTrue(isFirstWriteLocked);

        // Attempt to acquire another write lock
        boolean isSecondWriteLocked = readWriteLock.writeLock().tryLock();
        Assertions.assertFalse(isSecondWriteLocked);

        lockService.deleteEntity(keyName);
    }

    /**
     * 测试多线程并发的读写锁
     */
    @Test
    void testConcurrentReadWriteLock() throws InterruptedException {
        String keyName = "test_concurrent_read_write";
        DistributedReadWriteLock readWriteLock = lockFactory.newLock(keyName, LockType.MySQLReentrantReadWriteLock);
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

        ReentrantReadWriteLockEntity entity = lockRepository.findByLockKey(keyName);
        if (entity != null) {
            Assertions.assertEquals(0, entity.getReadLockCount());
            Assertions.assertEquals(0, entity.getWriteLockCount());
        }

        lockService.deleteEntity(keyName);
    }

}
