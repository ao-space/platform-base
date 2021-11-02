package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@RequestScoped
@Path("/v1/api")
@Tag(name = "Platform Registry Service",
    description = "Provides box and client registry related APIs.")
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
  @Operation(description =
      "初始化注册盒子，建立盒子与客户端的绑定关系，成功后返回盒子和客户端的注册码，以及网络相关的服务器信息。")
  public RegistryResult registry(@Valid RegistryInfo registryInfo,
                                 @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    final boolean validBoxUUID = registryService.isValidBoxUUID(registryInfo.getBoxUUID());
    if (!validBoxUUID) {
      throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
    }

    final Optional<RegistryEntity> rop = registryService.findByBoxUUID(registryInfo.getBoxUUID());
    if (rop.isEmpty()) {
      final TunnelServer server = TunnelServer.of(
          properties.getRegistryTunnelServerBaseUrl(), properties.getRegistryTunnelServerPort(),
          TunnelServer.Auth.of("n/a", "n/a"));
      // box 注册 & 管理员 client 注册
      String subDomain = registryInfo.getSubdomain() + "." + properties.getRegistrySubdomain();
      final RegistryEntity reClient = registryService.createRegistry(registryInfo, registryInfo.getClientUUID(), subDomain);
      return RegistryResult.of(reClient.getClientRegKey(), reClient.getBoxRegKey(), subDomain, server);
    } else {
      throw new WebApplicationException(
          "box uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
    }
  }

  @Logged
  @POST
  @Path("/registry/client")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "注册新客户端，建立盒子与客户端的绑定关系，成功后返回客户端的注册码，以及网络相关的服务器信息。")
  public ClientRegistryResult registryClient(@Valid ClientRegistryInfo registryInfo,
                                       @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    // 校验boxUuid有效性
    final boolean validBoxUUID = registryService.isValidBoxUUID(registryInfo.getBoxUUID());
    if (!validBoxUUID) {
      LOG.warnv("invalid box uuid, boxUuid:{0}", registryInfo.getBoxUUID());
      throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
    }
    // 校验boxUuid是否存在
    final List<RegistryEntity> registryEntityList = registryService.findAllByBoxUUIDAndBoxRegKey(registryInfo.getBoxUUID(),
            registryInfo.getBoxRegKey());
    if (registryEntityList.isEmpty()) {
      LOG.warnv("box uuid had not registered, boxUuid:{0}", registryInfo.getBoxUUID());
      throw new WebApplicationException("box uuid had not registered. Pls register box.", Response.Status.FORBIDDEN);
    }
    // 过滤clientUuid
    Optional<RegistryEntity> clientRegistryOp = registryEntityList.stream()
            .filter(entity -> entity.getClientUUID().equals(registryInfo.getClientUUID()))
            .findFirst();
    if (clientRegistryOp.isPresent()) {
      LOG.warnv("client uuid had already registered, boxUuid:{0}, clientUuid:{1}", registryInfo.getBoxUUID(),
              registryInfo.getClientUUID());
      throw new WebApplicationException("client uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
    }

    final TunnelServer server = TunnelServer.of(
            properties.getRegistryTunnelServerBaseUrl(), properties.getRegistryTunnelServerPort(),
            TunnelServer.Auth.of("n/a", "n/a"));
    final RegistryEntity re = registryService.createClientRegistry(registryEntityList.get(0), registryInfo.getClientUUID());
    return ClientRegistryResult.of(re.getClientRegKey(), registryEntityList.get(0).getSubdomain(), server);
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
      throw new WebApplicationException("invalid registry reset info", Response.Status.FORBIDDEN);
    }
  }

  @Logged
  @POST
  @Path("/registry/client/reset")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "重置客户端绑定关系，重置后可以为客户端重新注册绑定关系。")
  public ClientRegistryResetResult clientReset(@Valid ClientRegistryResetInfo clientResetInfo,
                                   @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    final boolean isExist = registryService.verifyClient(clientResetInfo.getClientRegKey(), clientResetInfo.getBoxUUID(),
            clientResetInfo.getClientUUID());
    if (isExist) {
      registryService.deleteByClientUUID(clientResetInfo.getBoxUUID(), clientResetInfo.getClientUUID());
      return ClientRegistryResetResult.of(clientResetInfo.getBoxUUID(), clientResetInfo.getClientUUID());
    } else {
      LOG.warnv("client uuid had not registered, boxUuid:{0}, clientUuid:{1}", clientResetInfo.getBoxUUID(),
              clientResetInfo.getClientUUID());
      throw new WebApplicationException("invalid client registry reset info", Response.Status.FORBIDDEN);
    }
  }

  @Logged
  @GET
  @Path("/registry/verify/box")
  @Operation(description = "校验盒子合法性。")
  public Response verifyBox(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                            @NotBlank @QueryParam("box_reg_key") @Schema(description = "盒子的注册 key。") String boxRegKey) {
    final boolean match = registryService.verifyBox(boxRegKey, boxUUID);
    if (!match) {
      throw new WebApplicationException("invalid registry box verify info", Response.Status.FORBIDDEN);
    }
    return Response.ok().build();
  }

  @Logged
  @GET
  @Path("/registry/verify/client")
  @Operation(description = "校验客户端合法性。")
  public Response verifyClient(@NotBlank @QueryParam("box_uuid") @Schema(description = "盒子的 uuid。") String boxUUID,
                               @NotBlank @QueryParam("client_uuid") @Schema(description = "客户端的 uuid。") String clientUUID,
                               @NotBlank @QueryParam("client_reg_key") @Schema(description = "客户端的注册 key。") String clientRegKey) {
    final boolean match = registryService.verifyClient(clientRegKey, boxUUID, clientUUID);
    if (!match) {
      throw new WebApplicationException("invalid registry client verify info", Response.Status.FORBIDDEN);
    }
    return Response.ok().build();
  }
}
