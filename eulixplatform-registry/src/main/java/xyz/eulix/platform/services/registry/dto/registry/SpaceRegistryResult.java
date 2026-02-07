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
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Simplified space registration result DTO.
 * Returns all necessary information for the client to connect.
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SpaceRegistryResult {
    @Schema(description = "Box UUID")
    private String boxUUID;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Client UUID")
    private String clientUUID;

    @Schema(description = "Assigned subdomain")
    private String subdomain;

    @Schema(description = "Full user domain for accessing the space")
    private String userDomain;

    @Schema(description = "User type: user_admin or user_member")
    private String userType;

    @Schema(description = "Network client credentials for NAT traversal")
    private NetworkClient networkClient;
}
