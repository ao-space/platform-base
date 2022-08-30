package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.dto.registry.v2.*;
import xyz.eulix.platform.services.registry.dto.registry.v2.BoxRegistryResult;
import xyz.eulix.platform.services.registry.dto.registry.v2.ClientRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.v2.ClientRegistryResult;
import xyz.eulix.platform.services.registry.dto.registry.v2.SubdomainGenResult;
import xyz.eulix.platform.services.registry.dto.registry.v2.UserRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.v2.UserRegistryResult;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Registry Service", description = "注册APIv2，包括网络资源管控、域名管理等")
public class RegistryResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Logged
    @POST
    @Path("/boxes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "注册盒子，成功后返回network client等信息")
    public BoxRegistryResult registryBox(@Valid BoxRegistryInfo boxRegistryInfo,
                                         @HeaderParam("Request-Id") @NotBlank String reqId,
                                         @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey) {
        return BoxRegistryResult.of("boxUUID", NetworkClient.of("clientId", "clientSecretKey"));
    }

    @Logged
    @DELETE
    @Path("/boxes/{box_uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "删除盒子注册信息")
    public void resetBox(@HeaderParam("Request-Id") @NotBlank String reqId,
                         @HeaderParam("Box-Reg-Key") String boxRegKey,
                         @PathParam("box_uuid") @NotBlank String boxUUID) {
        registryService.resetBox(boxUUID);
    }

    @RolesAllowed("admin")
    public void resetForce(String boxUUID) {
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
        return UserRegistryResult.of("boxUUID", "userId", "userDomain", "userType", "clientUUID");
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
        return ClientRegistryResult.of();
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
        return SubdomainGenResult.of();
    }

    @Logged
    @PUT
    @Path("/boxes/{box_uuid}/users/{user_id}/subdomain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "更新用户subdomain。幂等设计，建议client失败重试3次")
    public SubdomainUpdateResult subdomainUpdate(@Valid SubdomainUpdateInfo subdomainUpdateInfo,
                                                 @HeaderParam("Request-Id") @NotBlank String reqId,
                                                 @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                                 @PathParam("box_uuid") @NotBlank String boxUUID,
                                                 @PathParam("user_id") @NotBlank String userId) {
        return new SubdomainUpdateResult();
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
                              @HeaderParam("Box-Reg-Key") String boxRegKey,
                              @PathParam("box_uuid") @NotBlank String boxUUID) {
        LOG.infov("reset box forcely, boxUuid:{0}", boxUUID);
        registryService.resetBox(boxUUID);
    }
}
