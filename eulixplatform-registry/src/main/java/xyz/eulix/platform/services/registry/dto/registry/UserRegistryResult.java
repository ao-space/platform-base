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

/**
 * Used to define a data transfer object as REST response for holding related result
 * of activation result.
 */
@Data(staticConstructor = "of")
public class UserRegistryResult {
  @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问")
  private final String userDomain;

  @Schema(description = "用户的注册码，用于后续平台对于用户访问合法性的验证")
  private final String userRegKey;

  @Schema(description = "客户端的注册码，用于后续平台对于客户端访问合法性的验证")
  private final String clientRegKey;
}
