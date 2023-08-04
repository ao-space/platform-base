package xyz.eulix.platform.services.lock;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.lock.service.ReentrantLockService;

import java.util.concurrent.TimeUnit;

public class MySQLReentrantLock implements DistributedLock {

    private static final Logger LOG = Logger.getLogger("app.log");

    private ReentrantLockService lockService;

    // 锁的key(资源的唯一标识)
    private String keyName;

    // 拥有锁的线程uuid
    private String lockValue;

    // 锁超时时间  单位: s
    private Integer timeout;

    public MySQLReentrantLock(ReentrantLockService lockService, String keyName, String lockValue, Integer timeout) {
        this.lockService = lockService;
        this.keyName = keyName;
        this.lockValue = lockValue;
        this.timeout = timeout * 1000;
    }

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
                LOG.debugv("acquire lock success, keyName:{0}", keyName);
                return true;
            }
            // 等待后继续尝试获取
            if (sleepTime < 1000L) {
                sleepTime = sleepTime << 1;
            }
            LOG.debugv("acquire lock fail, retry after: {0}ms", sleepTime);
            Thread.sleep(sleepTime);
            end = System.currentTimeMillis();
        } while (end-start < unit.toMillis(waitTime));
        LOG.debugv("acquire lock timeout, elapsed: {0}ms", System.currentTimeMillis() - start);
        return false;
    }

    @Override
    public boolean tryLock() {
        lockService.deleteExpiredLock(keyName);
        return lockService.tryLock(keyName, lockValue, timeout);
    }

    @Override
    public void unlock() {
        lockService.releaseLock(keyName, lockValue, timeout);
    }
}
