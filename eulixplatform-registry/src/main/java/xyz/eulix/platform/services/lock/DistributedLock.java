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

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    /**
     * 在有效时间内阻塞加锁，可被中断
     */
    boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException;

    /**
     * 尝试加锁
     */
    boolean tryLock();

    /**
     * 解锁操作
     */
    void unlock();
}
