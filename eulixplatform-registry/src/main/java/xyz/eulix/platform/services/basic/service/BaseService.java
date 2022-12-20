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

package xyz.eulix.platform.services.basic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;
import xyz.eulix.platform.services.basic.dto.PlatformApis;
import xyz.eulix.platform.services.config.ApplicationProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.*;

@ApplicationScoped
public class BaseService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;
    @Inject
    OperationUtils utils;

    @Produces
    @SuppressWarnings("unused") // Used by DIC framework
    public PlatformApis apis() {
        PlatformApis routers;
        try{
            String json = utils.getStringFromFile(properties.getPlatformApisLocation());
            routers = utils.jsonToObject(json, PlatformApis.class);
        } catch (JsonProcessingException e) {
            LOG.errorv("platform apis created failed, processing json: " , e);
            throw new ServiceOperationException(ServiceError.API_JSON_EXCEPTION);
        } catch (IOException e) {
            LOG.errorv("platform apis created failed: read file " , e);
            throw new ServiceOperationException(ServiceError.API_IO_EXCEPTION);
        }

        return routers;
    }
}
