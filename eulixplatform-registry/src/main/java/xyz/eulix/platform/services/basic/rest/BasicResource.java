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

package xyz.eulix.platform.services.basic.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.basic.dto.PlatformApiResults;
import xyz.eulix.platform.services.basic.dto.PlatformApis;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.common.support.model.StatusResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Basic Service", description = "基础APIv2.")
public class BasicResource {

    @Inject
    ApplicationProperties properties;
    @Inject
    PlatformApis platformApis;

    @Logged
    @GET
    @Path("/ability")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询空间平台能力")
    public PlatformApiResults ability(@NotBlank @HeaderParam("Request-Id") String requestId) {
        List<PlatformApis.PlatformApi> platformApiResult = new ArrayList<>();

        for (var services : platformApis.getServices().entrySet()) {
            for (var api :services.getValue().entrySet()) {
                platformApiResult.add(api.getValue());
            }
        }
        return PlatformApiResults.of(platformApiResult);
    }

    @Logged
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询空间平台状态")
    public StatusResult status(@NotBlank @HeaderParam("Request-Id") String requestId) {
        return StatusResult.of("ok", properties.getVersion());
    }
}
