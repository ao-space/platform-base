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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Simplified space registration request DTO.
 * Combines box, user, and client registration into one step.
 */
@Data
public class SpaceRegistryInfo {
    @NotBlank
    @Schema(description = "Box UUID")
    private String boxUUID;

    @NotBlank
    @Schema(description = "User ID")
    private String userId;

    @NotBlank
    @Schema(description = "Client UUID")
    private String clientUUID;

    @Schema(description = "Preferred subdomain (optional, auto-generated if not provided)")
    @Pattern(regexp = "^[a-z][a-z0-9]{5,19}$", message = "Subdomain must start with a letter, contain only lowercase letters and numbers, and be 6-20 characters long")
    private String subdomain;

    @Schema(description = "User type: user_admin or user_member, defaults to user_admin")
    private String userType;
}
