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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户域名映射信息
 */
@Data
public class UserDomainRouteInfo {
    @NotBlank
    @Schema(description = "当前userId")
    private String userId;

    @NotBlank
    @Schema(description = "重定向 userDomain")
    @Pattern(regexp = "^[a-z][a-z0-9]{5,19}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62}){1,5}\\.?")
    private String userDomainRedirect;
}
