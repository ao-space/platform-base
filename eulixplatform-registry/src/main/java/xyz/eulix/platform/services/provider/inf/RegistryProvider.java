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

package xyz.eulix.platform.services.provider.inf;

/**
 * 注册相关Provider，可自定义实现
 */
public interface RegistryProvider {
    /**
     * 盒子身份认证
     *
     * @param boxUUID boxUUID
     * @return 是否合法
     */
    Boolean isBoxIllegal(String boxUUID);

    /**
     * 计算network路由
     *
     * @param networkClientId networkClientId
     * @return networkServerId
     */
    Long calculateNetworkRoute(String networkClientId);

    /**
     * 验证子域名是否合法
     *
     * @param subdomain 子域名
     * @return 是否合法
     */
    Boolean isSubdomainIllegal(String subdomain);
}
