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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.services.registry.dto.registry.SpaceRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.SpaceRegistryResult;
import xyz.eulix.platform.services.registry.service.RegistryService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Simplified Space Registration API.
 * Provides a one-step registration process for personal deployment scenarios.
 */
@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Space Service", description = "Simplified space registration API for personal deployment")
public class SpaceResource {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RegistryService registryService;

    @Logged
    @POST
    @Path("/spaces")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "One-step space registration. Registers box, user, and client in a single call. " +
            "Returns network credentials and user domain for immediate use.")
    public SpaceRegistryResult registerSpace(
            @Valid SpaceRegistryInfo spaceInfo,
            @HeaderParam("Request-Id") @NotBlank String reqId) {
        LOG.infov("Space registration request: boxUUID={0}, userId={1}, clientUUID={2}, subdomain={3}",
                spaceInfo.getBoxUUID(), spaceInfo.getUserId(), spaceInfo.getClientUUID(), spaceInfo.getSubdomain());
        return registryService.registerSpace(spaceInfo);
    }

    @Logged
    @DELETE
    @Path("/spaces/{box_uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete space registration. Removes box, all users, clients, and subdomains.")
    public Response deleteSpace(
            @PathParam("box_uuid") @NotBlank String boxUUID,
            @HeaderParam("Request-Id") @NotBlank String reqId) {
        LOG.infov("Space deletion request: boxUUID={0}", boxUUID);
        boolean exists = registryService.hasBoxRegistered(boxUUID);
        if (!exists) {
            LOG.warnv("Box not registered: boxUUID={0}", boxUUID);
            throw new WebApplicationException("Box not registered", Response.Status.NOT_FOUND);
        }
        registryService.resetBox(boxUUID);
        return Response.noContent().build();
    }

    @Logged
    @GET
    @Path("/spaces/{box_uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get space registration details including users, clients, and subdomains.")
    public Response getSpace(
            @PathParam("box_uuid") @NotBlank String boxUUID,
            @HeaderParam("Request-Id") @NotBlank String reqId) {
        LOG.infov("Space query request: boxUUID={0}", boxUUID);
        return Response.ok(registryService.boxRegistryBindUserAndClientInfo(boxUUID)).build();
    }
}
