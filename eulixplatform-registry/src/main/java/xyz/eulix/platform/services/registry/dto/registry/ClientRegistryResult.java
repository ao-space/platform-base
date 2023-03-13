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

package xyz.eulix.platform.services.registry.dto.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class ClientRegistryResult {

    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "用户的 ID")
    private String userId;

    @Schema(description = "客户端的 UUID")
    private String clientUUID;

    @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth")
    private String clientType;
}
