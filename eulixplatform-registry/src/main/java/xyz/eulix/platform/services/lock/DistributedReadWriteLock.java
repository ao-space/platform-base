package xyz.eulix.platform.services.lock;


/**
 * @author VvV
 * @date 2023/8/4
 */
public interface DistributedReadWriteLock {
    /**
     * Returns the lock used for reading.
     */
    DistributedLock readLock(String lockValue);

    /**
     * Returns the lock used for writing.
     */
    DistributedLock writeLock(String lockValue);
}
