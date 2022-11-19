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
 * Network Server Route 缓存键值对
 */
@Setter
@Getter
public class NSRoute {
    @Schema(description = "用户域名")
    private String userDomain;

    @Schema(description = "network server 地址 & network client id")
    private String networkInfo;

    public NSRoute(String userDomain, String networkInfo) {
        this.userDomain = userDomain;
        this.networkInfo = networkInfo;
    }
}