package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v1/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Registry Service",
    description = "Provides box and client registry related APIs.")
public class RegistryResource {

  @Inject
  RegistryService registryService;

  @Logged
  @POST
  @Path("/registry")

  @Operation(description =
      "Tries to registry a box and client, and meanwhile returns the reg_keys and tunnel server info")
  public RegistryResult registry(@Valid RegistryInfo registryInfo,
                                 @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {

    return RegistryResult.of(
        "crk_s92Szxk",
        "brk_98skLwf",
        registryInfo.getSubdomain() + ".space.eulix.xyz",
        TunnelServer.of(
            "https://ts.platform.eulix.xyz", 7000, TunnelServer.Auth.of("", ""))
    );
  }

  @Logged
  @POST
  @Path("/registry/reset")
  @Operation(description =
      "Reset the registry operation by a specified previous box.")
  public RegistryResetResult reset(@Valid RegistryResetInfo resetInfo,
                                   @Valid @HeaderParam("Request-Id") @NotBlank String reqId) {
    return RegistryResetResult.of(resetInfo.getBoxUUID());
  }
}
