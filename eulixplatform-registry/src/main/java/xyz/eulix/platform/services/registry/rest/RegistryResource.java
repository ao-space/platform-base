package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.RegistryClientEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import xyz.eulix.platform.services.token.service.TokenService;

@RequestScoped
@Path("/platform/v1/api")
@Tag(name = "Platform Registry Service", description = "Provides related APIs.")
public class RegistryResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Inject
    TokenService tokenService;

    @Logged
    @POST
    @Path("/registry/box")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册盒子，成功后返回盒子的注册码，以及network client信息。")
    public BoxRegistryResult registryBox(@Valid BoxRegistryInfo boxRegistryInfo,
                                         @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 校验boxUUID合法性
        registryService.isValidBoxUUIDThrowEx(boxRegistryInfo.getBoxUUID());
        // 校验盒子是否已注册
        registryService.hasBoxRegistered(boxRegistryInfo.getBoxUUID());
        // 注册&路由
        return registryService.registryBox(boxRegistryInfo);
    }

    @Logged
    @POST
    @Path("/registry/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册用户，同步注册客户端，成功后返回用户、客户端的注册码。")
    public UserRegistryResult registryUser(@Valid UserRegistryInfo userRegistryInfo,
                                           @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 参数校验
        RegistryTypeEnum userType = RegistryTypeEnum.fromValue(userRegistryInfo.getUserType());
        if (!RegistryTypeEnum.USER_ADMIN.equals(userType) && !RegistryTypeEnum.USER_MEMBER.equals(userType)) {
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "userRegistryInfo.userType");
        }
        // 校验盒子是否未注册
        registryService.hasBoxNotRegisteredThrow(userRegistryInfo.getBoxUUID(), userRegistryInfo.getBoxRegKey());
        // 校验用户是否已注册
        registryService.hasUserRegistered(userRegistryInfo.getBoxUUID(), userRegistryInfo.getUserId());
        SubdomainEntity subdomainEntity;
        if (CommonUtils.isNullOrEmpty(userRegistryInfo.getSubdomain())) {
            // 申请subdomain
            subdomainEntity = registryService.subdomainGen(userRegistryInfo.getBoxUUID());
            userRegistryInfo.setSubdomain(subdomainEntity.getSubdomain());
        } else {
            // 校验subdomain是否不存在，或者已使用
            subdomainEntity = registryService.isSubdomainNotExistOrUsed(userRegistryInfo.getSubdomain());
        }
        // 注册
        return registryService.registryUser(userRegistryInfo, subdomainEntity.getUserDomain());
    }

    @Logged
    @POST
    @Path("/registry/client")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册新客户端，成功后返回客户端的注册码。")
    public ClientRegistryResult registryClient(@Valid ClientRegistryInfo clientInfo,
                                               @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 参数校验
        RegistryTypeEnum clientType = RegistryTypeEnum.fromValue(clientInfo.getClientType());
        if (!RegistryTypeEnum.CLIENT_BIND.equals(clientType) && !RegistryTypeEnum.CLIENT_AUTH.equals(clientType)) {
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "clientRegistryInfo.clientType");
        }
        // 校验用户是否未注册
        registryService.hasUserNotRegistered(clientInfo.getBoxUUID(), clientInfo.getUserId(), clientInfo.getUserRegKey());
        // 校验client是否已注册
        registryService.hasClientRegistered(clientInfo.getBoxUUID(), clientInfo.getUserId(), clientInfo.getClientUUID());
        // 注册
        RegistryClientEntity clientEntity = registryService.registryClient(clientInfo.getBoxUUID(), clientInfo.getUserId(), clientInfo.getClientUUID(),
                RegistryTypeEnum.fromValue(clientInfo.getClientType()));
        return ClientRegistryResult.of(clientEntity.getClientRegKey());
    }

    @Logged
    @POST
    @Path("/registry/reset/box")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置盒子绑定关系，重置后可以为盒子重新注册绑定关系。")
    public BoxRegistryResetResult resetBox(@Valid BoxRegistryResetInfo boxResetInfo,
                                           @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        Boolean notRegistered = registryService.hasBoxNotRegistered(boxResetInfo.getBoxUUID(), boxResetInfo.getBoxRegKey());
        if (notRegistered) {
            LOG.warnv("box uuid had not registered, boxUuid:{0}", boxResetInfo.getBoxUUID());
            throw new WebApplicationException("invalid box registry reset info", Response.Status.FORBIDDEN);
        }
        registryService.resetBox(boxResetInfo.getBoxUUID());
        return BoxRegistryResetResult.of(boxResetInfo.getBoxUUID());
    }

    @Logged
    @POST
    @Path("/registry/reset/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置盒子绑定关系，重置后可以为盒子重新注册绑定关系。")
    public UserRegistryResetResult resetUser(@Valid UserRegistryResetInfo userResetInfo,
                                         @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        final boolean match = registryService.verifyUser(userResetInfo.getUserRegKey(), userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        if (!match) {
            LOG.warnv("user id had not registered, boxUuid:{0}, userId:{1}",userResetInfo.getBoxUUID(), userResetInfo.getUserId());
            throw new WebApplicationException("invalid user registry reset info", Response.Status.FORBIDDEN);
        }
        registryService.resetUser(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        return UserRegistryResetResult.of(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
    }

    @Logged
    @POST
    @Path("/registry/reset/client")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置客户端绑定关系，重置后可以为客户端重新注册绑定关系。")
    public ClientRegistryResetResult resetClient(@Valid ClientRegistryResetInfo clientResetInfo,
                                                 @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        final boolean isExist = registryService.verifyClient(clientResetInfo.getClientRegKey(), clientResetInfo.getBoxUUID(),
                clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
        if (!isExist) {
            LOG.warnv("client uuid had not registered, boxUuid:{0}, userId:{1}, clientUuid:{2}", clientResetInfo.getBoxUUID(),
                    clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
            throw new WebApplicationException("invalid client registry reset info", Response.Status.FORBIDDEN);
        }
        registryService.deleteClientByClientUUID(clientResetInfo.getBoxUUID(), clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
        return ClientRegistryResetResult.of(clientResetInfo.getBoxUUID(), clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/registry/reset/force")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "强制重置盒子绑定关系，需管理员权限。")
    public BoxRegistryResetResult resetForce(@Valid BoxRegistryForceReset resetInfo,
                                             @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        LOG.warnv("reset box forcely, boxUuid:{0}", resetInfo.getBoxUUID());
        registryService.resetBox(resetInfo.getBoxUUID());
        return BoxRegistryResetResult.of(resetInfo.getBoxUUID());
    }

    @Logged
    @GET
    @Path("/registry/verify/box")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "校验盒子合法性。")
    public Response verifyBox(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                              @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey) {
        Boolean notRegistered = registryService.hasBoxNotRegistered(boxUUID, boxRegKey);
        if (notRegistered) {
            LOG.warnv("invalid registry box verify info, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid registry box verify info", Response.Status.FORBIDDEN);
        }
        return Response.ok().build();
    }

    @Logged
    @GET
    @Path("/registry/verify/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "校验用户合法性。")
    public Response verifyUser(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                               @NotBlank @QueryParam("user_id") @Schema(description = "用户的 id。") String userId,
                               @NotBlank @QueryParam("user_reg_key") @Schema(description = "用户的注册 key。") String userRegKey) {
        final boolean match = registryService.verifyUser(userRegKey, boxUUID, userId);
        if (!match) {
            LOG.warnv("invalid registry box verify info, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid registry box verify info", Response.Status.FORBIDDEN);
        }
        return Response.ok().build();
    }

    @Logged
    @GET
    @Path("/registry/verify/client")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "校验客户端合法性。")
    public Response verifyClient(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                                 @NotBlank @QueryParam("user_id") @Schema(description = "用户的 id。") String userId,
                                 @NotBlank @QueryParam("client_uuid") @Schema(description = "客户端的 uuid。") String clientUUID,
                                 @NotBlank @QueryParam("client_reg_key") @Schema(description = "客户端的注册 key。") String clientRegKey) {
        final boolean match = registryService.verifyClient(clientRegKey, boxUUID, userId, clientUUID);
        if (!match) {
            LOG.warnv("invalid registry client verify info, boxUuid:{0}, userId:{1}, clientUuid:{2}", boxUUID, userId, clientUUID);
            throw new WebApplicationException("invalid registry client verify info", Response.Status.FORBIDDEN);
        }
        return Response.ok().build();
    }

    @Logged
    @GET
    @Path("/subdomain/gen")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "分发全局唯一的subdomain。")
    public SubdomainGenResult subdomainGen(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                                       @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey,
                                       @QueryParam("effective_time") @Max(604800) @Schema(description = "有效期，单位秒，最长7天") Integer effectiveTime) {
        // 校验 box 身份
        registryService.hasBoxNotRegisteredThrow(boxUUID, boxRegKey);
        // 校验数量上限
        registryService.reachUpperLimit(boxUUID);
        // 生成
        SubdomainEntity subdomainEntity = registryService.subdomainGen(boxUUID, effectiveTime);
        return SubdomainGenResult.of(boxUUID, subdomainEntity.getSubdomain());
    }

    @Logged
    @PUT
    @Path("/subdomain/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "更新subdomain。幂等设计，建议client失败重试3次。")
    public SubdomainUpdateResult subdomainUpdate(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                                           @NotBlank @QueryParam("user_id") @Schema(description = "用户的 id。") String userId,
                                           @NotBlank @QueryParam("user_reg_key") @Schema(description = "用户的注册 key。") String userRegKey,
                                           @NotBlank @QueryParam("subdomain") @Size(max = 100) @Schema(description = "子域名，最长100字符") String subdomain) {
        // 校验用户是否未注册
        registryService.hasUserNotRegistered(boxUUID, userId, userRegKey);
        // 更新域名
        return registryService.subdomainUpdate(boxUUID, userId, subdomain);
    }

    @RolesAllowed("admin")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registry/boxinfo")
    @Operation(description = "已注册信息查询")
    public BoxRegistryDetailInfo registriesBoxInfos(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                          @NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID) {
        return registryService.boxRegistryBindUserAndClientInfo(boxUUID);
    }

    @Logged
    @GET
    @Path("/registry/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取注册用户信息")
    public UserRegistryDetail getRegistryUser(@HeaderParam("Request-Id") @NotBlank String reqId,
        @NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
        @NotBlank @QueryParam("user_id") @Schema(description = "用户的 id。") String userId,
        @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校检用户
        registryService.hasUserNotRegistered(boxUUID, userId);
        var registryUserEntity = registryService.getRegistryUserEntity(boxUUID, userId);
        var subdomainEntity = registryService.getSubdomainEntity(boxUUID, userId);

        return  UserRegistryDetail.of(subdomainEntity.getSubdomain(), registryUserEntity.getUserRegKey());
    }

    @Logged
    @GET
    @Path("/registry/client")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取注册客户端信息")
    public ClientRegistryDetail getRegistryClient(@HeaderParam("Request-Id") @NotBlank String reqId,
        @NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
        @NotBlank @QueryParam("user_id") @Schema(description = "用户的 id。") String userId,
        @NotBlank @QueryParam("client_uuid") @Schema(description = "客户端的 uuid。") String clientUUID,
        @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey) {
        // 验证 box reg key 有效期
        tokenService.verifyBoxRegKey(boxUUID, boxRegKey);
        // 校检 client
        registryService.hasClientNotRegistered(boxUUID, userId, clientUUID);

        var registryClientEntity = registryService.getRegistryClientEntity(boxUUID,
            userId, clientUUID);

        return ClientRegistryDetail.of(registryClientEntity.getClientRegKey());
    }
}
