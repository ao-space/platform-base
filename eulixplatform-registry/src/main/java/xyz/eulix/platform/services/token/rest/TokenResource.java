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

package xyz.eulix.platform.services.token.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.services.token.dto.*;
import xyz.eulix.platform.services.token.entity.BoxTokenEntity;
import xyz.eulix.platform.services.token.service.TokenService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Token Service", description = "BoxRegKey管理")
public class TokenResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    TokenService tokenService;

    @Logged
    @POST
    @Path("/auth/box_reg_keys")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取box_reg_keys")
    public TokenResults createTokens(@Valid TokenInfo tokenInfo,
                                     @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 验证签名
        tokenService.verifySign(tokenInfo);
        // 生成 box reg key
        ArrayList<TokenResult> tokenResults = tokenService.createBoxTokens(tokenInfo);
        return TokenResults.of(tokenInfo.getBoxUUID(), tokenResults);
    }

    @Logged
    @POST
    @Path("/auth/box_reg_key/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "校验box_reg_key有效性")
    public CheckTokenResult checkToken(@Valid CheckTokenInfo tokenInfo,
                                       @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 验证 box reg key
        BoxTokenEntity boxTokenEntity = tokenService.verifyBoxRegKey(tokenInfo.getBoxUUID(), tokenInfo.getBoxRegKey(),
                ServiceEnum.fromValue(tokenInfo.getServiceId()));
        return CheckTokenResult.of(tokenInfo.getBoxUUID(), tokenInfo.getServiceId(), boxTokenEntity.getServiceName(),
                boxTokenEntity.getExpiresAt());
    }
}
