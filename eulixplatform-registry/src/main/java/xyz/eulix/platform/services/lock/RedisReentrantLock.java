/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.services.lock;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisReentrantLock implements DistributedLock {
    private static final Logger LOG = Logger.getLogger("app.log");

    private RedisClient redisClient;

    private String keyName;

    private String lockValue;

    private Integer timeout;    //锁超时时间

    public RedisReentrantLock(RedisClient redisClient, String keyName, String lockValue, Integer timeout) {
        this.redisClient = redisClient;
        this.keyName = keyName;
        this.lockValue = lockValue;
        this.timeout = timeout * 1000;      // 单位ms
    }

    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        long start = System.currentTimeMillis();
        long end;
        long sleepTime = 1L; // 重试间隔时间，单位ms。指数增长，最大值为1024ms
        do {
            //尝试获取锁
            boolean success = tryLock(keyName, lockValue, timeout);
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

    public boolean tryLock() {
        return tryLock(keyName, lockValue, timeout);
    }

    public void unlock() {
        releaseLock(keyName, lockValue, timeout);
    }

    /**
     * 通过exists判断，如果锁不存在，则设置值和过期时间，加锁成功
     * 通过hexists判断，如果锁已存在，并且锁的是当前线程，则证明是重入锁，加锁成功
     * 如果锁已存在，但锁的不是当前线程，则证明有其他线程持有锁。返回当前锁的过期时间，加锁失败
     *
     * @param key     key
     * @param value   value
     * @param timeout 超时时间
     * @return 是否加锁成功
     */
    private boolean tryLock(String key, String value, Integer timeout) {
        String command =
                "if (redis.call('exists', KEYS[1]) == 0) then " +                   //判断指定的key是否存在
                    "redis.call('hset', KEYS[1], ARGV[2], 1); " +                   //新增key，value为hash结构
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +                   //设置过期时间
                    "return nil; " +                                                //直接返回null，表示加锁成功
                "end; " +
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +         //判断hash中是否存在指定的建
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +                //hash中指定键的值+1
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +                   //重置过期时间
                    "return nil; " +                                                //返回null，表示加锁成功
                "end; " +
                "return redis.call('pttl', KEYS[1]);";                              //返回key的剩余过期时间，表示加锁失败
        List<String> list = new ArrayList<>();
        list.add(command);
        list.add("1");
        list.add(key);
        list.add(timeout.toString());
        list.add(value);
        Response result = redisClient.eval(list);
        if (result == null) {
            LOG.debugv("acquire lock success, keyName:{0}, lockValue:{1}, timeout:{2}", key, value, timeout);
            return true;
        } else {
            LOG.debugv("acquire lock fail, keyName:{0}, lockValue:{1}, ttl:{2}", key, value, result.toInteger());
            return false;
        }
    }

    /**
     * 如果锁已经不存在，通过publish发布锁释放的消息，解锁成功
     * 如果解锁的线程和当前锁的线程不是同一个，解锁失败，抛出异常
     * 通过hincrby递减1，先释放一次锁。若剩余次数还大于0，则证明当前锁是重入锁，刷新过期时间；若剩余次数小于0，删除key并发布锁释放的消息，解锁成功
     *
     * @param key     key
     * @param value   value
     * @param timeout 超时时间
     * @return
     */
    private void releaseLock(String key, String value, Integer timeout) {
        String command =
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
                    "return nil;" +                                                         //判断当前客户端之前是否已获取到锁，若没有直接返回null
                "end; " +
                "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " +           //锁重入次数-1
                "if (counter > 0) then " +                                                  //若锁尚未完全释放，需要重置过期时间
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return 0; " +                                                          //返回0表示锁未完全释放
                "else " +
                    "redis.call('del', KEYS[1]); " +                                        //若锁已完全释放，删除当前key
                    "return 1; " +                                                          //返回1表示锁已完全释放
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
            LOG.debugv("release lock sucess, keyName:{0}, lockValue:{1}", key, value);
        } else {
            LOG.debugv("Decrease lock times sucess, keyName:{0}, lockValue:{1}", key, value);
        }
    }

}
