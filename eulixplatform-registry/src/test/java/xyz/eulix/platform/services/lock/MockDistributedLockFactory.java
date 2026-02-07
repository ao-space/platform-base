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

import io.quarkus.test.Mock;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.lock.service.ReentrantLockService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Mock DistributedLockFactory for tests - uses in-memory locks for Redis, real MySQL locks.
 */
@Mock
@ApplicationScoped
public class MockDistributedLockFactory extends DistributedLockFactory {
    private static final Logger LOG = Logger.getLogger("app.log");

    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Inject
    ReentrantLockService mysqlLockService;

    @Inject
    ApplicationProperties applicationProperties;

    @Override
    public DistributedLock newLock(String keyName, LockType lockType) {
        LOG.debugv("[Mock] Creating lock, key:{0}, type:{1}", keyName, lockType);

        // Use real MySQL implementation for MySQL locks
        if (lockType.equals(LockType.MySQLReentrantLock)) {
            String lockValue = UUID.randomUUID().toString();
            Integer timeout = applicationProperties.getLockExpireTime();
            return new MySQLReentrantLock(mysqlLockService, keyName, lockValue, timeout);
        }

        // Use in-memory mock for Redis locks
        return new MockDistributedLock(keyName);
    }

    private static class MockDistributedLock implements DistributedLock {
        private static final Logger LOG = Logger.getLogger("app.log");
        private final String keyName;
        private final ReentrantLock lock;

        public MockDistributedLock(String keyName) {
            this.keyName = keyName;
            this.lock = locks.computeIfAbsent(keyName, k -> new ReentrantLock());
        }

        @Override
        public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
            boolean acquired = lock.tryLock(waitTime, unit);
            LOG.debugv("[Mock] tryLock with timeout, key:{0}, acquired:{1}", keyName, acquired);
            return acquired;
        }

        @Override
        public boolean tryLock() {
            boolean acquired = lock.tryLock();
            LOG.debugv("[Mock] tryLock, key:{0}, acquired:{1}", keyName, acquired);
            return acquired;
        }

        @Override
        public void unlock() {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                LOG.debugv("[Mock] unlock, key:{0}", keyName);
            }
        }
    }

    // Utility method to clear all locks (useful for test cleanup)
    public static void clearAll() {
        locks.clear();
    }
}
