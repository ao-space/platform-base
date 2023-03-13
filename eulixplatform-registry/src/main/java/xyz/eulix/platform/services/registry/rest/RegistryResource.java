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

package xyz.eulix.platform.services.registry.rest;

import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.common.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import xyz.eulix.platform.services.registry.dto.registry.ClientRegistryResult;
import xyz.eulix.platform.services.registry.entity.RegistryClientEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.token.service.TokenService;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Registry Service", description = "注册APIv2，包括网络资源管控、域名管理等")
public class RegistryResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Inject
    TokenService tokenService;

    @Logged
    @POST
    @Path("/boxes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册盒子，成功后返回network client等信息")
    public BoxRegistryResult registryBox(@Valid BoxRegistryInfo boxRegistryInfo,
                                         @HeaderParam("Request-Id") @NotBlank String reqId,
                                         @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey) {
        // 验证 box reg key 有效期
        var boxTokenEntity = tokenService.verifyRegistryBoxRegKey(boxRegistryInfo.getBoxUUID(), boxRegKey);
        return registryService.registryBox(boxTokenEntity, CommonUtils.getUUID());
    }

    @Logged
    @DELETE
    @Path("/boxes/{box_uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除盒子注册信息")
    public void resetBox(@HeaderParam("Request-Id") @NotBlank String reqId,
                         @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                         @PathParam("box_uuid") @NotBlank String boxUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        final boolean isExist = registryService.hasBoxRegistered(boxUUID);
        if (!isExist) {
            LOG.warnv("box uuid had not registered, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid box registry reset info", Status.NOT_FOUND);
        }
        registryService.resetBox(boxUUID);
    }

    @Logged
    @POST
    @Path("/boxes/{box_uuid}/users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册用户，同步注册绑定客户端")
    public UserRegistryResult registryUser(@Valid UserRegistryInfo userRegistryInfo,
                                           @HeaderParam("Request-Id") @NotBlank String reqId,
                                           @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                           @PathParam("box_uuid") @NotBlank String boxUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        // 校检盒子
        registryService.hasBoxNotRegisteredThrow(boxUUID);
        // 注册
        return registryService.registryUser(userRegistryInfo, boxUUID);
    }

    @Logged
    @DELETE
    @Path("/boxes/{box_uuid}/users/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除用户注册信息")
    public void resetUser(@HeaderParam("Request-Id") @NotBlank String reqId,
                          @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                          @PathParam("box_uuid") @NotBlank String boxUUID,
                          @PathParam("user_id") @NotBlank String userId) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        final boolean match = registryService.verifyUser(boxUUID, userId);
        if (!match) {
            LOG.warnv("user id had not registered, boxUuid:{0}, userId:{1}",boxUUID, userId);
            throw new WebApplicationException("invalid user registry reset info", Status.NOT_FOUND);
        }
        registryService.resetUser(boxUUID, userId);
    }

    @Logged
    @POST
    @Path("/boxes/{box_uuid}/users/{user_id}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册客户端")
    public ClientRegistryResult registryClient(@Valid ClientRegistryInfo clientInfo,
                                               @HeaderParam("Request-Id") @NotBlank String reqId,
                                               @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                               @PathParam("box_uuid") @NotBlank String boxUUID,
                                               @PathParam("user_id") @NotBlank String userId) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        // 校检用户
        registryService.hasUserNotRegistered(boxUUID, userId);
        RegistryClientEntity clientEntity = registryService.registryClient(boxUUID, userId, clientInfo.getClientUUID(),
                RegistryTypeEnum.fromValue(clientInfo.getClientType()));
        return ClientRegistryResult.of(clientEntity.getBoxUUID(), clientEntity.getUserId(),
                clientEntity.getClientUUID(), clientEntity.getRegistryType());
    }

    @Logged
    @DELETE
    @Path("/boxes/{box_uuid}/users/{user_id}/clients/{client_uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除客户端注册信息")
    public void resetClient(@HeaderParam("Request-Id") @NotBlank String reqId,
                            @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                            @PathParam("box_uuid") @NotBlank String boxUUID,
                            @PathParam("user_id") @NotBlank String userId,
                            @PathParam("client_uuid") @NotBlank String clientUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        final boolean isExist = registryService.verifyClient(boxUUID, userId, clientUUID);
        if (!isExist) {
            LOG.warnv("client uuid had not registered, boxUuid:{0}, userId:{1}, clientUuid:{2}", boxUUID, userId, clientUUID);
            throw new WebApplicationException("invalid client registry reset info", Status.NOT_FOUND);
        }
        registryService.deleteClientByClientUUID(boxUUID, userId, clientUUID);
    }

    @Logged
    @POST
    @Path("boxes/{box_uuid}/subdomains")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "申请subdomain，平台保证全局唯一性")
    public SubdomainGenResult subdomainGen(@Valid SubdomainGenInfo subdomainGenInfo,
                                           @HeaderParam("Request-Id") @NotBlank String reqId,
                                           @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                           @PathParam("box_uuid") @NotBlank String boxUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        // 校验 box 身份
        registryService.hasBoxNotRegisteredThrow(boxUUID);
        // 校验数量上限
        registryService.reachUpperLimit(boxUUID);

        SubdomainEntity subdomainEntity = registryService.subdomainGen(boxUUID, subdomainGenInfo.getEffectiveTime());
        return SubdomainGenResult.of(boxUUID, subdomainEntity.getSubdomain(), subdomainEntity.getExpiresAt());
    }

    @Logged
    @PUT
    @Path("/boxes/{box_uuid}/users/{user_id}/subdomain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "更新用户subdomain。幂等设计")
    public SubdomainUpdateResult subdomainUpdate(@Valid SubdomainUpdateInfo subdomainUpdateInfo,
                                                 @HeaderParam("Request-Id") @NotBlank String reqId,
                                                 @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                                 @PathParam("box_uuid") @NotBlank String boxUUID,
                                                 @PathParam("user_id") @NotBlank String userId) {
        // 验证 box reg key 有效期
        tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        // 校验用户是否未注册
        registryService.hasUserNotRegistered(boxUUID, userId);
        return registryService.subdomainUpdate(boxUUID, userId, subdomainUpdateInfo.getSubdomain());
    }
}
