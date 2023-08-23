package xyz.eulix.platform.services.lock;

/**
 * @author VvV
 * @date 2023/7/27
 */
public enum LockType {

    RedisReentrantLock,

    MySQLReentrantLock,

    MySQLReentrantReadWriteLock,

    RedisReentrantReadWriteLock
}
