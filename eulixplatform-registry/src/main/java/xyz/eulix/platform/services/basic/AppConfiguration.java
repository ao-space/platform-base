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

package xyz.eulix.platform.services.basic;

import io.quarkus.runtime.Startup;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.repository.RegistryBoxEntityRepository;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Startup
@ApplicationScoped
public class AppConfiguration {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryBoxEntityRepository registryBoxEntityRepository;

    @PostConstruct
    void init() {
        LOG.infov("Application init begin...");

        /*
         * 更新 network secret 为 hash结果
         * 适用版本：2.0.0
         */
        LOG.infov("Update Network Secret Key begin...");
        // 查询全部已注册盒子
        AtomicInteger successBoxCount = new AtomicInteger(0);
        AtomicInteger failureBoxCount = new AtomicInteger(0);
        List<RegistryBoxEntity> registryBoxEntityList = registryBoxEntityRepository.listAll();
        registryBoxEntityList.stream()
                .filter(registryBoxEntity -> CommonUtils.isNullOrEmpty(registryBoxEntity.getNetworkSecretSalt()))
                .forEach(registryBoxEntity -> {
                    // 为已注册盒子生成盐值，并重新计算secret key
                    try {
                        // 随机字符串做盐
                        String salt = CommonUtils.getUUID();
                        String secretKey = registryBoxEntity.getNetworkSecretKey();
                        // 盐和密码结合取hash值
                        String hashSecretKey = DigestUtils.md5Hex(salt + secretKey);
                        registryBoxEntityRepository.updateSecretKeyAndSaltByBoxUUID(hashSecretKey, salt, registryBoxEntity.getBoxUUID());
                        LOG.infov("Update Network Secret Key success, boxuuid:{0}, from:{1} ,to:{2}", registryBoxEntity.getBoxUUID(),
                                secretKey, hashSecretKey);
                        successBoxCount.incrementAndGet();
                    } catch (Exception e) {
                        LOG.errorv(e, "Update Network Secret Key failed, boxuuid:{0}", registryBoxEntity.getBoxUUID());
                        failureBoxCount.incrementAndGet();
                    }
                });

        LOG.infov("Update Network Secret Key success! Total:{0}, success:{1}, failure:{2}", registryBoxEntityList.size(),
                successBoxCount.get(), failureBoxCount.get());
        LOG.infov("Application init succeed!");
    }
}
