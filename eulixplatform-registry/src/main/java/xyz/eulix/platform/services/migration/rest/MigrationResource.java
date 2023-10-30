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

package xyz.eulix.platform.services.migration.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;
import xyz.eulix.platform.services.lock.DistributedLock;
import xyz.eulix.platform.services.lock.DistributedLockFactory;
import xyz.eulix.platform.services.lock.LockType;
import xyz.eulix.platform.services.migration.dto.*;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.services.migration.service.MigrationService;
import xyz.eulix.platform.services.token.service.TokenService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Migration Service", description = "割接APIv2.")
public class MigrationResource {

    @Inject
    TokenService tokenService;
    @Inject
    MigrationService migrationService;
    @Inject
    DistributedLockFactory lockFactory;
    private static final Logger LOG = Logger.getLogger("app.log");

    @Logged
    @POST
    @Path("boxes/{box_uuid}/migration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "空间平台割接")
    public BoxMigrationResult migration(@Valid BoxMigrationInfo boxMigrationInfo,
                                        @HeaderParam("Request-Id") @NotBlank String reqId,
                                        @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                        @PathParam("box_uuid") @NotBlank String boxUUID) {
        var boxTokenEntity = tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);

        DistributedLock lock = lockFactory.newLock(boxUUID, LockType.RedisReentrantLock);
        // 加锁
        boolean isLocked = lock.tryLock();
        if (isLocked) {
            LOG.infov("migration in acquire lock success, boxUUID:{0}", boxUUID);
            try {
                return migrationService.migration(boxMigrationInfo, boxTokenEntity);
            } finally {
                // 释放锁
                lock.unlock();
                LOG.infov("migration in release lock success, boxUUID:{0}", boxUUID);
            }
        } else {
            LOG.infov("migration in acquire lock fail, boxUUID:{0}", boxUUID);
            throw new ServiceOperationException(ServiceError.MIGRATION_IN_LOCK_ERROR);
        }

    }

    @Logged
    @POST
    @Path("boxes/{box_uuid}/route")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "域名重定向")
    public MigrationRouteResult migrationRoute(@Valid MigrationRouteInfo migrationRouteInfo,
                                               @HeaderParam("Request-Id") @NotBlank String reqId,
                                               @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                               @PathParam("box_uuid") @NotBlank String boxUUID) {
        var boxTokenEntity = tokenService.verifyRegistryBoxRegKey(boxUUID, boxRegKey);
        DistributedLock lock = lockFactory.newLock(boxUUID, LockType.RedisReentrantLock);
        // 加锁
        boolean isLocked = lock.tryLock();
        if (isLocked) {
            LOG.infov("migration out acquire lock success, boxUUID:{0}", boxUUID);
            try {
                return migrationService.migrationRoute(migrationRouteInfo, boxTokenEntity);
            } finally {
                // 释放锁
                lock.unlock();
                LOG.infov("migration out release lock success, boxUUID:{0}", boxUUID);
            }
        } else {
            LOG.infov("migration out acquire lock fail, boxUUID:{0}", boxUUID);
            throw new ServiceOperationException(ServiceError.MIGRATION_OUT_LOCK_ERROR);
        }
    }
}
