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
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.lock.service.ReentrantLockService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class DistributedLockFactory {
    @Inject
    RedisClient redisClient;

    @Inject
    ReentrantLockService mysqlLockService;

    @Inject
    ApplicationProperties applicationProperties;

    public DistributedLock newLock(String keyName, LockType lockType) {
        String lockValue = UUID.randomUUID().toString();
        Integer timeout = applicationProperties.getLockExpireTime();    // 单位s

        if (lockType.equals(LockType.RedisReentrantLock)) {
            return new RedisReentrantLock(redisClient, keyName, lockValue, timeout);
        } else if(lockType.equals(LockType.MySQLReentrantLock)) {
            return new MySQLReentrantLock(mysqlLockService, keyName, lockValue, timeout);
        } else {
            throw new IllegalArgumentException("Invalid lock type: " + lockType);
        }

    }
}
