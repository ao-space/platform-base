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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
@JsonIgnoreProperties
public class BoxInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @Schema(description = "盒子的描述信息")
    private String desc;

    @Schema(description = "盒子的扩展信息,json格式")
    private Object extra;

    @Schema(description = "盒子的公钥，authType=box_pub_key时必传")
    private String boxPubKey;

    @Schema(description = "盒子的authType: box_uuid/box_pub_key")
    @ValueOfEnum(enumClass = AuthTypeEnum.class, valueMethod = "getName")
    private String authType;

    @Schema(description = "盒子是否已注册")
    private boolean isRegistered;

    @Schema(description = "操作时间")
    private OffsetDateTime updatedAt;
}
