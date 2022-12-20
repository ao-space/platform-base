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

/*
package xyz.eulix.platform.services.lock;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class DistributedLockTest {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    DistributedLockFactory lockFactory;

    @Test
    void testLock() {
        String keyName = "ditributedLockKey1";
        DistributedLock lock = lockFactory.newLock(keyName);
        // 加锁
        Boolean isLocked = lock.tryLock();
        if (isLocked) {
            LOG.infov("acquire lock success, keyName:{0}", keyName);
            try {
                // 这里写需要处理业务的业务代码
                LOG.info("do something.");
            } finally {
                // 释放锁
                lock.unlock();
                LOG.infov("release lock success, keyName:{0}", keyName);
            }
        } else {
            LOG.infov("acquire lock fail, keyName:{0}", keyName);
        }
        Assertions.assertTrue(isLocked);
    }
}
*/