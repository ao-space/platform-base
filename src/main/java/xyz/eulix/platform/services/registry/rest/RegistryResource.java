package xyz.eulix.platform.services.registry.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.service.ActivationRegistryService;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Registry Service",
    description = "Provides box and client registry related APIs.")
public class RegistryResource {

  @Inject
  ActivationRegistryService registryService;

  @Logged
  @POST
  @Path("/registry")
  @Operation(description =
      "Tries to registry a box and client, and meanwhile returns the reg_keys and tunnel server info")
  public RegistryResult registry(@Valid RegistryInfo registryInfo) {

    return RegistryResult.of(
        "ck_s92Szxk",
        "bk_98skLwf",
        TunnelServer.of(
            "https://ts.platform.eulix.xyz", 7000, TunnelServer.Auth.of("", ""))
    );
  }

  @Logged
  @POST
  @Path("/registry/reset")
  @Operation(description =
      "Reset the registry operation by a specified previous box.")
  public RegistryResetResult reset(@Valid RegistryResetInfo resetInfo) {
    return RegistryResetResult.of(resetInfo.getBoxUUID());
  }
}
