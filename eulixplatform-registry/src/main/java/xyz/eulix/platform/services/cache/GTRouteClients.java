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

import java.util.Set;

/**
 * 以 `Redis sets` 的形式在 redis 存储
 * key - 表名：SpaceUUIDs， 字段： boxUUID+userId
 * value - `sets` 中存储的元素为 client-uuid
 */
@Setter
@Getter
public class GTRouteClients {
    @Schema(description = "boxUUID")
    private String boxUUID;

    @Schema(description = "userId")
    private String userId;

    @Schema(description = "clientUUIDs")
    private Set<String> clientUUIDs;

    public GTRouteClients(String boxUUID, String userId,  Set<String> clientUUIDs) {
        this.boxUUID = boxUUID;
        this.userId = userId;
        this.clientUUIDs = clientUUIDs;
    }
}