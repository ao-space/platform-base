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

package xyz.eulix.platform.services.cache;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * key - 表名： GTR, 字段：空间的用户 subdomain
 * value - json 格式，包括主要字段如下：
 * - gtSvrNode - 目标域名, 即 network server （GT）node，如 `eulixnetwork-server.bp-cicada-rc.svc.cluster.local:80`
 * - gtCliId - network-client 配对的id， 如 `fae1e91d8ccf464daa1b3d0d9e3509fa`
 * - redirectDomain - 重定向的用户域名, 如 `b5r9qd8h.myself.com`
 * - redirectDomainStatus - 重定向域名状态，1表示正常，2表示已过期
 * - boxUUID - 盒子设备的 boxUUID
 * - userId - 盒子上用户对外的 id
 */
@Setter
@Getter
public class GTRouteBasic {
    @Schema(description = "用户subdomain")
    private String subdomain;

    @Schema(description = "network server 地址 & network client id等基础数据")
    private NetworkBasic networkBasic;

    public GTRouteBasic(String subdomain, NetworkBasic networkBasic) {
        this.subdomain = subdomain;
        this.networkBasic = networkBasic;
    }
}