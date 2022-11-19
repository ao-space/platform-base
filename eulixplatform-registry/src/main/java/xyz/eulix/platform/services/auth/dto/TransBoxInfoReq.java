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

package xyz.eulix.platform.services.auth.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TransBoxInfoReq {
    // 二维码pkey值
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9-]{36}")
    @Schema(pattern = "[a-zA-Z0-9-]{36}", description = "二维码pkey值")
    private String pkey;

    // 登录box端的key
    @NotBlank
    @Size(max = 128)
    @Schema(maxLength = 128 ,description = "盒子侧bkey值")
    private String bkey;

    @NotBlank
    @Size(max = 128)
    @Schema(maxLength = 128 ,description = "用户域名")
    private String userDomain;

    // 盒子公钥
    @NotBlank
    @Size(max = 1024)
    @Schema(maxLength = 1024 ,description = "盒子公钥")
    private String boxPubKey;
}
