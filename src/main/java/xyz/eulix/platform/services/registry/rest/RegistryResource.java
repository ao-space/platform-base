package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
@Path("/v1/api")
@Tag(name = "Platform Registry Service", description = "Provides related APIs.")
public class RegistryResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Inject
    ApplicationProperties properties;

    @Logged
    @POST
    @Path("/registry")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册盒子，同步注册管理员、客户端，成功后返回盒子、用户、客户端的注册码，以及network client信息。")
    public RegistryResult registry(@Valid RegistryInfo registryInfo,
                                   @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        // 校验boxUUID合法性
        registryService.isValidBoxUUIDThrowEx(registryInfo.getBoxUUID());
        // 校验盒子是否已注册
        registryService.hasBoxRegistered(registryInfo.getBoxUUID());
        // 校验subdomain是否已存在
        if (!CommonUtils.isNullOrEmpty(registryInfo.getSubdomain())) {
            registryService.isSubdomainExist(registryInfo.getSubdomain());
        }
        // 注册&路由
        return registryService.registryBox(registryInfo);
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
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "userType");
        }
        // 校验盒子是否未注册
        registryService.hasBoxNotRegistered(userRegistryInfo.getBoxUUID(), userRegistryInfo.getBoxRegKey());
        // 校验用户是否已注册
        registryService.hasUserRegistered(userRegistryInfo.getBoxUUID(), userRegistryInfo.getUserId());
        // 校验subdomain是否已存在
        if (!CommonUtils.isNullOrEmpty(userRegistryInfo.getSubdomain())) {
            registryService.isSubdomainExist(userRegistryInfo.getSubdomain());
        }
        // 注册
        return registryService.registryUser(userRegistryInfo);
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
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "clientType");
        }
        // 校验用户是否未注册
        List<RegistryEntity> userEntitys = registryService.findAllByUserIDAndUserRegKey(clientInfo.getBoxUUID(), clientInfo.getUserId(),
                clientInfo.getUserRegKey());
        if (userEntitys.isEmpty()) {
            LOG.warnv("invalid user registry info, boxUUID:{0}, userId:{1}", clientInfo.getBoxUUID(), clientInfo.getUserId());
            throw new WebApplicationException("invalid user registry info.", Response.Status.FORBIDDEN);
        }
        // 校验client是否已注册
        registryService.hasClientRegistered(clientInfo.getBoxUUID(), clientInfo.getUserId(), clientInfo.getClientUUID());
        // 注册
        RegistryEntity clientEntity = registryService.registryClient(clientInfo.getBoxUUID(), userEntitys.get(0).getBoxRegKey(), clientInfo.getUserId(),
                clientInfo.getUserRegKey(), clientInfo.getClientUUID(), RegistryTypeEnum.fromValue(clientInfo.getClientType()));
        return ClientRegistryResult.of(clientEntity.getClientRegKey());
    }

    @Logged
    @POST
    @Path("/registry/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置盒子绑定关系，重置后可以为盒子重新注册绑定关系。")
    public RegistryResetResult reset(@Valid RegistryResetInfo resetInfo,
                                     @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        final boolean match = registryService.verifyBox(resetInfo.getBoxRegKey(), resetInfo.getBoxUUID());
        if (match) {
            registryService.deleteByBoxUUID(resetInfo.getBoxUUID());
            return RegistryResetResult.of(resetInfo.getBoxUUID());
        } else {
            LOG.warnv("box uuid had not registered, boxUuid:{0}", resetInfo.getBoxUUID());
            throw new WebApplicationException("invalid box registry reset info", Response.Status.FORBIDDEN);
        }
    }

    @Logged
    @POST
    @Path("/registry/user/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置盒子绑定关系，重置后可以为盒子重新注册绑定关系。")
    public UserRegistryResetResult resetUser(@Valid UserRegistryResetInfo userResetInfo,
                                         @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        final boolean match = registryService.verifyUser(userResetInfo.getUserRegKey(), userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        if (match) {
            registryService.deleteByUserId(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
            return UserRegistryResetResult.of(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        } else {
            LOG.warnv("user id had not registered, boxUuid:{0}, userId:{1}",userResetInfo.getBoxUUID(), userResetInfo.getUserId());
            throw new WebApplicationException("invalid user registry reset info", Response.Status.FORBIDDEN);
        }
    }

    @Logged
    @POST
    @Path("/registry/client/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "重置客户端绑定关系，重置后可以为客户端重新注册绑定关系。")
    public ClientRegistryResetResult resetClient(@Valid ClientRegistryResetInfo clientResetInfo,
                                                 @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        final boolean isExist = registryService.verifyClient(clientResetInfo.getClientRegKey(), clientResetInfo.getBoxUUID(),
                clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
        if (isExist) {
            registryService.deleteByClientUUID(clientResetInfo.getBoxUUID(), clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
            return ClientRegistryResetResult.of(clientResetInfo.getBoxUUID(), clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
        } else {
            LOG.warnv("client uuid had not registered, boxUuid:{0}, userId:{1}, clientUuid:{1}", clientResetInfo.getBoxUUID(),
                    clientResetInfo.getUserId(), clientResetInfo.getClientUUID());
            throw new WebApplicationException("invalid client registry reset info", Response.Status.FORBIDDEN);
        }
    }

    @RolesAllowed("admin")
    @Logged
    @POST
    @Path("/registry/reset/force")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "强制重置盒子绑定关系，需管理员权限。")
    public RegistryResetResult resetForce(@Valid RegistryForceReset resetInfo,
                                          @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
        LOG.warnv("reset box forcely, boxUuid:{0}", resetInfo.getBoxUUID());
        registryService.deleteByBoxUUID(resetInfo.getBoxUUID());
        return RegistryResetResult.of(resetInfo.getBoxUUID());
    }

    @Logged
    @GET
    @Path("/registry/verify/box")
    @Operation(description = "校验盒子合法性。")
    public Response verifyBox(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                              @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey) {
        final boolean match = registryService.verifyBox(boxRegKey, boxUUID);
        if (!match) {
            LOG.warnv("invalid registry box verify info, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid registry box verify info", Response.Status.FORBIDDEN);
        }
        return Response.ok().build();
    }

    @Logged
    @GET
    @Path("/registry/verify/user")
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
}
