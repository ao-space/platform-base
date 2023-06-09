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

package xyz.eulix.platform.services.auth.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.auth.dto.GenPkeyRsp;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.entity.PkeyAuthEntity;
import xyz.eulix.platform.services.auth.service.AuthService;
import xyz.eulix.platform.common.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 鉴权类
 */
@ApplicationScoped
@Path("/v2/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Auth Service", description = "扫码登录APIv2")
public class AuthResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    AuthService authService;

    @POST
    @Path("/pkeys")
    @Logged
    @Operation(description = "生成pkey，被授权端调用")
    public GenPkeyRsp pkeyGen(@NotBlank @HeaderParam("Request-Id") String requestId) {
        PkeyAuthEntity pkeyAuthEntity = authService.genPkey();
        return GenPkeyRsp.of(pkeyAuthEntity.getPkey(), pkeyAuthEntity.getExpiresAt());
    }

    @POST
    @Path("/pkeys/{pkey}/boxinfo")
    @Logged
    @Operation(description = "发送盒子公钥，授权端调用")
    public PollPkeyRsp boxinfoTrans(@NotBlank @HeaderParam("Request-Id") String requestId,
                                    @PathParam("pkey") @NotBlank @Pattern(regexp = "[a-zA-Z0-9-]{36}")
                                    @Parameter(description = "二维码pkey值。[a-zA-Z0-9-]{36}") String pkey,
                                    @Valid TransBoxInfoReq boxInfoReq) {
        return authService.savePkeyAuth(pkey, boxInfoReq);
    }

    @GET
    @Path("/pkeys/{pkey}/boxinfo")
    @Logged
    @Operation(description = "获取盒子公钥，被授权端调用")
    public PollPkeyRsp pkeyPoll(@NotBlank @HeaderParam("Request-Id") String requestId,
                                @PathParam("pkey") @NotBlank @Pattern(regexp = "[a-zA-Z0-9-]{36}")
                                @Parameter(description = "二维码pkey值。[a-zA-Z0-9-]{36}") String pkey) {
        return authService.pollPkeyAuth(pkey);
    }
}
