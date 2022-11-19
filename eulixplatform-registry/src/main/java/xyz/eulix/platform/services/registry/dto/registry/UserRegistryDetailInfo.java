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

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Data(staticConstructor = "of")
public class UserRegistryDetailInfo {
  @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问")
  private final String userDomain;

  @Schema(description = "子域名")
  private final String subDomain;

  @Schema(description = "注册类型")
  private final String userType;

  @Schema(description = "用户id")
  private final String userId;

  @Schema(description = "创建时间")
  private final OffsetDateTime createdAt;

  @Schema(description = "client相关信息")
  private final List<ClientRegistryDetailInfo> clientInfos;
}
