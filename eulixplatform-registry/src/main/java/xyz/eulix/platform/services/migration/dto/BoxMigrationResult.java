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

package xyz.eulix.platform.services.migration.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.services.registry.dto.registry.NetworkClient;

import java.util.List;

/**
 *  盒子割接结果
 */
@Data(staticConstructor = "of")
public class BoxMigrationResult {
    @Schema(description = "盒子的 UUID")
    private final String boxUUID;

    @Schema(description = "为盒子分配的 network client 信息")
    private final NetworkClient networkClient;

    @Schema(description = "用户列表")
    private final List<UserMigrationInfo> userInfos;
}
