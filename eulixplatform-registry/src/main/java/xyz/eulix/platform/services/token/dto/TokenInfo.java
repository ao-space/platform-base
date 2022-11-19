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

package xyz.eulix.platform.services.token.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of registry information.
 */
@Data
public class TokenInfo {
    @NotBlank
    @Schema(description = "盒子的 UUID")
    private String boxUUID;

    @NotEmpty
    @Schema(description = "平台id：空间平台（serviceId=10001）、产品服务平台（serviceId=10002）")
    private List<@ValueOfEnum(enumClass = ServiceEnum.class, valueMethod = "getServiceId") String> serviceIds;

    @Schema(description = "签名，使用公钥验证盒子身份时必传")
    private String sign;
}
