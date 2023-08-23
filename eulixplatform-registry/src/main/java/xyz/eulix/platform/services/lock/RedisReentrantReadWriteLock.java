package xyz.eulix.platform.services.lock;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author VvV
 * @date 2023/8/22
 */
public class RedisReentrantReadWriteLock implements DistributedReadWriteLock {

    private static final Logger LOG = Logger.getLogger("app.log");

    private RedisClient redisClient;

    private String keyName;

    private Integer timeout;    //锁超时时间

    public RedisReentrantReadWriteLock(RedisClient redisClient, String keyName, Integer timeout) {
        this.redisClient = redisClient;
        this.keyName = keyName;
        this.timeout = timeout * 1000;      // 单位ms
    }


    /**
     * Returns the lock used for reading.
     */
    public DistributedLock readLock() {
        String lockValue = UUID.randomUUID().toString();
        return new ReadLock(lockValue);
    }

    /**
     * Returns the lock used for writing.
     */
    public DistributedLock writeLock() {
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
                    LOG.debugv("acquire lock success, keyName:{0}", keyName);
                    LOG.infov("acquire lock success, keyName:{0}", keyName);
                    return true;
                }
                // 等待后继续尝试获取
                if (sleepTime < 1000L) {
                    sleepTime = sleepTime << 1;
                }
                LOG.debugv("acquire lock fail, retry after: {0}ms", sleepTime);
                LOG.infov("acquire lock fail, retry after: {0}ms", sleepTime);
                Thread.sleep(sleepTime);
                end = System.currentTimeMillis();
            } while (end-start < unit.toMillis(waitTime));
            LOG.debugv("acquire lock timeout, elapsed: {0}ms", System.currentTimeMillis() - start);
            return false;
        }

        /**
         * 尝试加锁
         */
        @Override
        public boolean tryLock() {
            return tryLock(keyName, lockValue, timeout);
        }

        /**
         * 解锁操作
         */
        @Override
        public void unlock() {
            releaseLock(keyName, lockValue, timeout);
        }

        /**
         * 通过key获取锁的模式mode
         *     如果锁的键不存在  加读锁
         *     如果 mode=read 或者 (mode=write&&写锁是当前实例的)
         *         增加读锁记录 (对于第二种情况 写锁解锁时 需要特别处理 写锁释放完 则将锁模式设置为读锁)
         *     否则 加锁失败 返回锁剩余过期时间
         * @param key      锁唯一标识
         * @param value    实例(客户端)唯一uuid
         * @param timeout  过期时间
         * @return 加锁是否成功
         */
        private boolean tryLock(String key, String value, Integer timeout) {
            String command =
                    "local mode = redis.call('hget', KEYS[1], 'mode'); " +  // 获取锁的模式
                    "if (mode == false) then " +                            // 如果锁的键不存在
                    "    redis.call('hset', KEYS[1], 'mode', 'read'); " +   // 设置模式为读模式
                    "    redis.call('hset', KEYS[1], ARGV[2], 1); " +       // 创建新的键，值为1
                    "    redis.call('pexpire', KEYS[1], ARGV[1]); " +       // 设置过期时间
                    "    return nil; " +                                    // 返回 nil 表示加锁成功
                    "end; " +
                    "if (mode == 'read') or (mode == 'write' and redis.call('hexists', KEYS[1], ARGV[3]) == 1) then " + // 如果锁的模式为读或者锁的模式为写且被当前实例持有
                    "    redis.call('hincrby', KEYS[1], ARGV[2], 1); " +    // 将值加1
                    "    redis.call('pexpire', KEYS[1], ARGV[1]); " +       // 设置过期时间
                    "    return nil; " +                                    // 返回 nil 表示加锁成功
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);";                  // 返回剩余的过期时间

            List<String> list = new ArrayList<>();
            list.add(command);
            list.add("1");
            list.add(key);
            list.add(timeout.toString());
            list.add(value);
            list.add(value + ":write");
            Response result = redisClient.eval(list);
            if (result == null) {
                LOG.debugv("acquire read lock success, keyName:{0}, lockValue:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("acquire read lock fail, keyName:{0}, lockValue:{1}, ttl:{2}", key, value, result.toInteger());
                return false;
            }
        }

        /**
         * 通过key获取锁模式
         *     如果锁不存在 解锁成功
         * 检查当前实例是否持有锁
         *     如果当前实例不持有锁  抛出异常
         * 当前实例持有锁 减少锁重入次数
         *     如果重入次数变为0  删除当前实例的锁
         *     如果删除后 还有其他锁存在  重置过期时间
         *     如果删除后 没有其他锁存在 删除锁记录
         * @param key      锁唯一标识
         * @param value    实例(客户端)唯一uuid
         * @param timeout  过期时间
         */
        private void releaseLock(String key, String value, Integer timeout) {
            String command =
                    "local mode = redis.call('hget', KEYS[1], 'mode'); " +  // 获取锁的模式
                    "if (mode == false) then " +                            // 如果锁的键不存在
                    "    return 1; " +                                      // 返回1表示解锁成功
                    "end; " +
                    "local lockExists = redis.call('hexists', KEYS[1], ARGV[2]); " + // 检查当前实例是否持有锁
                    "if (lockExists == 0) then " +                          // 如果当前实例不持有锁
                    "    return nil; " +                                    // 返回nil
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " + // 减少当前实例的锁重入次数
                    "if (counter == 0) then " +                             // 如果重入次数变为0
                    "    redis.call('hdel', KEYS[1], ARGV[2]); " +          // 删除当前实例的锁
                    "end; " +
                    "if (redis.call('hlen', KEYS[1]) > 1) then " +          // 如果还有其他锁
                    "    redis.call('pexpire', KEYS[1], ARGV[1]); " +      // 重置锁过期时间
                    "    return 0; " +
                    "end; " +
                    "redis.call('del', KEYS[1]); " +                         // 删除锁
                    "return 1;";  // 返回1表示解锁成功
            List<String> list = new ArrayList<>();
            list.add(command);
            list.add("1");
            list.add(key);
            list.add(timeout.toString());
            list.add(value);
            Response result = redisClient.eval(list);
            if (result == null) {
                LOG.warnv("Current thread does not hold lock, keyName:{0}, lockValue:{1}", key, value);
                throw new RuntimeException("current thread does not hold lock");
            }
            Integer resultNum = result.toInteger();
            if (resultNum == 1) {
                LOG.debugv("release read lock success, keyName:{0}, lockValue:{1}", key, value);
            } else {
                LOG.debugv("Decrease read lock times success, keyName:{0}, lockValue:{1}", key, value);
            }
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

        /**
         * 尝试加锁
         */
        @Override
        public boolean tryLock() {
            return tryLock(keyName, lockValue, timeout);
        }

        /**
         * 解锁操作
         */
        @Override
        public void unlock() {
            releaseLock(keyName, lockValue, timeout);
        }

        /**
         * 通过key获取锁模式mode
         *     如果锁不存在  加写锁
         *     如果mode=write 检查当前线程是否持有锁
         *         是 增加重入次数、重置过期时间
         *         否 加锁失败
         * @param key      锁唯一标识
         * @param value    实例(客户端)唯一uuid
         * @param timeout  过期时间
         * @return 加锁是否成功
         */
        private boolean tryLock(String key, String value, Integer timeout) {
            String command =
                    "local mode = redis.call('hget', KEYS[1], 'mode'); " +   // 获取锁的模式
                    "if (mode == false) then " +                             // 如果锁的键不存在
                    "    redis.call('hset', KEYS[1], 'mode', 'write'); " +   // 设置锁的模式为写模式
                    "    redis.call('hset', KEYS[1], ARGV[2], 1); " +        // 设置当前线程的锁重入次数为1
                    "    redis.call('pexpire', KEYS[1], ARGV[1]); " +        // 设置锁的过期时间
                    "    return nil; " +                                     // 返回nil表示锁已经被当前线程获取
                    "end; " +
                    "if (mode == 'write') then " +                           // 如果是写锁模式
                    "    if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " + // 检查当前线程是否已经持有锁
                    "        redis.call('hincrby', KEYS[1], ARGV[2], 1); " + // 增加当前线程的锁重入次数
                    "        redis.call('pexpire', KEYS[1], ARGV[1]); " +    // 更新锁的过期时间
                    "        return nil; " +                                 // 返回nil表示锁已经被当前线程获取
                    "    end; " +
                    "end;" +
                    "return redis.call('pttl', KEYS[1]);";                   // 如果执行到这里，加锁失败，返回锁的剩余过期时间
            List<String> list = new ArrayList<>();
            list.add(command);
            list.add("1");
            list.add(key);
            list.add(timeout.toString());
            list.add(value);
            Response result = redisClient.eval(list);
            if (result == null) {
                LOG.debugv("acquire write lock success, keyName:{0}, lockValue:{1}, timeout:{2}", key, value, timeout);
                return true;
            } else {
                LOG.debugv("acquire write lock fail, keyName:{0}, lockValue:{1}, ttl:{2}", key, value, result.toInteger());
                return false;
            }
        }

        /**
         * 通过key获取锁模式
         *     如果锁不存在  解锁成功
         *     如果mode=write 检查当前实例是否持有锁
         *         否 抛出异常
         *         是 减少重入次数
         *             重入次数是否大于0
         *                 是 设置锁过期时间
         *                 否 删除当前实例的写锁 判断是否map中key的数量
         *                     key数量为1(该key为mode) 当前锁不被任何实例持有 删除整个锁记录
         *                     key数量不为1 将mode设置为read (这时当前实例持有读锁)
         * @param key      锁唯一标识
         * @param value    实例(客户端)唯一uuid
         * @param timeout  过期时间
         */
        private void releaseLock(String key, String value, Integer timeout) {
            String command =
                    "local mode = redis.call('hget', KEYS[1], 'mode'); " +  // 获取锁的模式
                    "if (mode == false) then " +                            // 如果锁的键不存在
                    "    return 1; " +                                      // 返回1表示解锁成功
                    "end; " +
                    "if (mode == 'write') then " +                          // 如果是写锁模式
                    "    local lockExists = redis.call('hexists', KEYS[1], ARGV[2]); " + // 检查当前实例是否持有锁
                    "    if (lockExists == 0) then " +                      // 如果当前线程不持有锁
                    "        return nil; " +                                // 返回nil
                    "    else " +
                    "        local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " + // 减少当前实例的锁重入次数
                    "        if (counter > 0) then " +                      // 如果重入次数大于0
                    "            redis.call('pexpire', KEYS[1], ARGV[1]); " + // 设置锁的过期时间
                    "            return 0; " +                              // 返回0表示锁还在有效期内
                    "        else " +
                    "            redis.call('hdel', KEYS[1], ARGV[2]); " +  // 删除当前实例的锁
                    "            if (redis.call('hlen', KEYS[1]) == 1) then " + // 如果还剩一个锁
                    "                redis.call('del', KEYS[1]); " +        // 删除锁
                    "            else " +
                    "                redis.call('hset', KEYS[1], 'mode', 'read'); " + // 设置锁模式为读模式
                    "            end; " +
                    "            return 1; " +                              // 返回1表示解锁成功
                    "        end; " +
                    "    end; " +
                    "end; " +
                    "return nil;";
            List<String> list = new ArrayList<>();
            list.add(command);
            list.add("1");
            list.add(key);
            list.add(timeout.toString());
            list.add(value);
            Response result = redisClient.eval(list);
            if (result == null) {
                LOG.warnv("Current thread does not hold lock, keyName:{0}, lockValue:{1}", key, value);
                throw new RuntimeException("current thread does not hold lock");
            }
            Integer resultNum = result.toInteger();
            if (resultNum == 1) {
                LOG.debugv("release write lock success, keyName:{0}, lockValue:{1}", key, value);
            } else {
                LOG.debugv("Decrease write lock times success, keyName:{0}, lockValue:{1}", key, value);
            }
        }
    }


}
