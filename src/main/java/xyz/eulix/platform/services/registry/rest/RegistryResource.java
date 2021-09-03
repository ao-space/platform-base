package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
import java.util.Optional;

@RequestScoped
@Path("/v1/api")
@Tag(name = "Platform Registry Service",
    description = "Provides box and client registry related APIs.")
public class RegistryResource {

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
      "初始化注册盒子，建立盒子与客户端的绑定关系，成功后返回盒子和客户端的注册码，以及网络相关的穿透服务器信息。")
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
      final RegistryEntity re = registryService.createRegistry(registryInfo, server);
      return RegistryResult.of(
          re.getClientRegKey(),
          re.getBoxRegKey(),
          re.getSubdomain(),
          server
      );
    } else {
      throw new WebApplicationException("box uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
    }
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
      registryService.deleteByBoxRegKey(resetInfo.getBoxRegKey());
      return RegistryResetResult.of(resetInfo.getBoxUUID());
    } else {
      throw new WebApplicationException("invalid registry reset info", Response.Status.FORBIDDEN);
    }
  }

  @Logged
  @GET
  @Path("/registry/verify/box")
  @Operation(description = "校验盒子合法性。")
  public Response verifyBox(@NotBlank @QueryParam("uuid") @Schema(description = "盒子的 uuid。") String uuid,
                            @NotBlank @QueryParam("key") @Schema(description = "盒子的注册 key。") String key) {
    final boolean match = registryService.verifyBox(key, uuid);
    if (!match) {
      throw new WebApplicationException("invalid registry box verify info", Response.Status.FORBIDDEN);
    }
    return Response.ok().build();
  }

  @Logged
  @GET
  @Path("/registry/verify/client")
  @Operation(description = "校验客户端合法性。")
  public Response verifyClient(@NotBlank @QueryParam("uuid") @Schema(description = "客户端的 uuid。") String uuid,
                               @NotBlank @QueryParam("key") @Schema(description = "客户端的注册 key。") String key) {
    final boolean match = registryService.verifyClient(key, uuid);
    if (!match) {
      throw new WebApplicationException("invalid registry client verify info", Response.Status.FORBIDDEN);
    }
    return Response.ok().build();
  }
}
