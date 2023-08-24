package xyz.eulix.platform.services.lock;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.lock.service.ReentrantReadWriteLockService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author VvV
 * @date 2023/8/4
 */
public class MySQLReentrantReadWriteLock implements DistributedReadWriteLock{

    private static final Logger LOG = Logger.getLogger("app.log");

    private ReentrantReadWriteLockService lockService;
    private String keyName;

    private int timeout;

    public MySQLReentrantReadWriteLock(ReentrantReadWriteLockService lockService, String keyName, int timeout) {
        this.lockService = lockService;
        this.keyName = keyName;
        this.timeout = timeout * 1000;
    }

    /**
     * Returns the lock used for reading.
     */
    @Override
    public MySQLReentrantReadWriteLock.ReadLock readLock() {
        String lockValue = UUID.randomUUID().toString();
        return new ReadLock(lockValue);
    }

    /**
     * Returns the lock used for writing.
     */
    @Override
    public MySQLReentrantReadWriteLock.WriteLock writeLock() {
        String lockValue = UUID.randomUUID() + ":write";
        return new WriteLock(lockValue);
    }

    public class ReadLock implements DistributedLock {

        private String lockValue;

        public ReadLock(String lockValue) {
            this.lockValue = lockValue;
        }

        public String getLockValue() {
            return lockValue;
        }

        /**
         * 在有效时间内阻塞加锁，可被中断
         *
         * @param waitTime
         * @param unit
         */
        @Override
        public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
            long start = System.currentTimeMillis();
            long end;
            long sleepTime = 1L; // 重试间隔时间，单位ms。指数增长，最大值为1024ms
            do {
                //尝试获取锁
                boolean success = tryLock();
                if (success) {
                    //成功获取锁，返回
                    LOG.debugv("acquire read lock success, keyName:{0}", keyName);
                    return true;
                }
                // 等待后继续尝试获取
                if (sleepTime < 1000L) {
                    sleepTime = sleepTime << 1;
                }
                LOG.debugv("acquire read lock fail, retry after: {0}ms", sleepTime);
                Thread.sleep(sleepTime);
                end = System.currentTimeMillis();
            } while (end-start < unit.toMillis(waitTime));
            LOG.debugv("acquire read lock timeout, elapsed: {0}ms", System.currentTimeMillis() - start);
            return false;
        }

        /**
         * 尝试加锁
         */
        @Override
        public boolean tryLock() {
            return lockService.tryReadLock(keyName, lockValue, timeout);
        }

        /**
         * 解锁操作
         */
        @Override
        public void unlock() {
            lockService.releaseReadLock(keyName, lockValue, timeout);
        }
    }

    public class WriteLock implements DistributedLock {

        private String lockValue;

        public WriteLock(String lockValue) {
            this.lockValue = lockValue;
        }

        public String getLockValue() {
            return lockValue;
        }

        /**
         * 在有效时间内阻塞加锁，可被中断
         *
         * @param waitTime
         * @param unit
         */
        @Override
        public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
            long start = System.currentTimeMillis();
            long end;
            long sleepTime = 1L; // 重试间隔时间，单位ms。指数增长，最大值为1024ms
            do {
                //尝试获取锁
                boolean success = tryLock();
                if (success) {
                    //成功获取锁，返回
                    LOG.debugv("acquire write lock success, keyName:{0}", keyName);
                    return true;
                }
                // 等待后继续尝试获取
                if (sleepTime < 1000L) {
                    sleepTime = sleepTime << 1;
                }
                LOG.debugv("acquire write lock fail, retry after: {0}ms", sleepTime);
                Thread.sleep(sleepTime);
                end = System.currentTimeMillis();
            } while (end-start < unit.toMillis(waitTime));
            LOG.debugv("acquire write lock timeout, elapsed: {0}ms", System.currentTimeMillis() - start);
            return false;
        }

        /**
         * 尝试加锁
         */
        @Override
        public boolean tryLock() {
            return lockService.tryWriteLock(keyName, lockValue, timeout);
        }

        /**
         * 解锁操作
         */
        @Override
        public void unlock() {
            lockService.releaseWriteLock(keyName, lockValue, timeout);
        }
    }

}
