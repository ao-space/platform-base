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

import java.util.List;

/**
 * 用户割接结果
 */
@Data(staticConstructor = "of")
public class UserMigrationResult {
    @Schema(description = "用户的 ID")
    private String userId;

    @Schema(description = "用户被指定的子域名字段")
    private String subdomain;

    @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
    private String userType;

    @Schema(description = "Client列表")
    private List<ClientMigrationResult> clientResults;
}
