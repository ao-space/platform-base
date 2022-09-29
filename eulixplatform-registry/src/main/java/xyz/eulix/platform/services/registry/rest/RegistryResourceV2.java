package xyz.eulix.platform.services.registry.rest;

import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryDetailInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainUpdateResult;
import xyz.eulix.platform.services.registry.dto.registry.v2.*;
import xyz.eulix.platform.common.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import xyz.eulix.platform.services.registry.entity.RegistryClientEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.token.service.TokenService;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Registry Service", description = "注册APIv2，包括网络资源管控、域名管理等")
public class RegistryResourceV2 {
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
    public BoxRegistryResultV2 registryBox(@Valid BoxRegistryInfo boxRegistryInfo,
                                           @HeaderParam("Request-Id") @NotBlank String reqId,
                                           @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey) {
        // 验证 box reg key 有效期
        var boxTokenEntity = tokenService.verifyBoxRegKey(boxRegistryInfo.getBoxUUID(), boxRegKey);
        return registryService.registryBoxV2(boxTokenEntity);
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
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        Boolean notRegistered = registryService.hasBoxNotRegistered(boxUUID, boxRegKey);
        if (notRegistered) {
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
    public UserRegistryResultV2 registryUser(@Valid UserRegistryInfoV2 userRegistryInfo,
                                             @HeaderParam("Request-Id") @NotBlank String reqId,
                                             @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                             @PathParam("box_uuid") @NotBlank String boxUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校检盒子
        registryService.hasBoxNotRegisteredThrow(boxUUID);
        // 注册
        return registryService.registryUserV2(userRegistryInfo, boxUUID);
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
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
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
    public ClientRegistryResultV2 registryClient(@Valid ClientRegistryInfoV2 clientInfo,
                                                 @HeaderParam("Request-Id") @NotBlank String reqId,
                                                 @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                                 @PathParam("box_uuid") @NotBlank String boxUUID,
                                                 @PathParam("user_id") @NotBlank String userId) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校检用户
        registryService.hasUserNotRegistered(boxUUID, userId);
        RegistryClientEntity clientEntity = registryService.registryClientV2(boxUUID, userId, clientInfo.getClientUUID(),
                RegistryTypeEnum.fromValue(clientInfo.getClientType()));
        return ClientRegistryResultV2.of(clientEntity.getBoxUUID(), clientEntity.getUserId(),
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
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
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
    public SubdomainGenResultV2 subdomainGen(@Valid SubdomainGenInfoV2 subdomainGenInfo,
                                             @HeaderParam("Request-Id") @NotBlank String reqId,
                                             @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                             @PathParam("box_uuid") @NotBlank String boxUUID) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校验 box 身份
        registryService.hasBoxNotRegisteredThrow(boxUUID);
        // 校验数量上限
        registryService.reachUpperLimit(boxUUID);

        SubdomainEntity subdomainEntity = registryService.subdomainGen(boxUUID, subdomainGenInfo.getEffectiveTime());
        return SubdomainGenResultV2.of(boxUUID, subdomainEntity.getSubdomain(), subdomainEntity.getExpiresAt());
    }

    @Logged
    @PUT
    @Path("/boxes/{box_uuid}/users/{user_id}/subdomain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "更新用户subdomain。幂等设计，建议client失败重试3次")
    public SubdomainUpdateResult subdomainUpdate(@Valid SubdomainUpdateInfoV2 subdomainUpdateInfo,
                                                 @HeaderParam("Request-Id") @NotBlank String reqId,
                                                 @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                                 @PathParam("box_uuid") @NotBlank String boxUUID,
                                                 @PathParam("user_id") @NotBlank String userId) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校验用户是否未注册
        registryService.hasUserNotRegistered(boxUUID, userId);
        return registryService.subdomainUpdate(boxUUID, userId, subdomainUpdateInfo.getSubdomain());
    }

    @RolesAllowed("admin")
    @Logged
    @GET
    @Path("/boxes/{box_uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "查询盒子信息")
    public BoxRegistryDetailInfo getBoxInfo(@HeaderParam("Request-Id") @NotBlank String reqId,
                                            @PathParam("box_uuid") @NotBlank String boxUUID) {
        return registryService.boxRegistryBindUserAndClientInfo(boxUUID);
    }

    @RolesAllowed("admin")
    @Logged
    @DELETE
    @Path("/boxes/{box_uuid}/force")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "强制删除盒子注册信息")
    public void resetBoxForce(@HeaderParam("Request-Id") @NotBlank String reqId,
                              @PathParam("box_uuid") @NotBlank String boxUUID) {
        LOG.infov("reset box forcely, boxUuid:{0}", boxUUID);
        final boolean isExist = registryService.isValidBoxUUID(boxUUID);
        if (!isExist) {
            LOG.warnv("box uuid had not registered, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid box registry reset info", Status.NOT_FOUND);
        }
        registryService.resetBox(boxUUID);
    }
}
