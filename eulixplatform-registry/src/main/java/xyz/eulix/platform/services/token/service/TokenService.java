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

package xyz.eulix.platform.services.token.service;

import static xyz.eulix.platform.services.token.dto.ServiceEnum.REGISTRY;

import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;
import xyz.eulix.platform.services.registry.entity.BoxInfoEntity;
import xyz.eulix.platform.services.token.dto.*;
import xyz.eulix.platform.services.token.entity.BoxTokenEntity;
import xyz.eulix.platform.services.registry.repository.BoxInfoEntityRepository;
import xyz.eulix.platform.services.token.repository.BoxTokenEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class TokenService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoEntityRepository boxInfoEntityRepository;

    @Inject
    BoxTokenEntityRepository boxTokenEntityRepository;

    @Inject
    OperationUtils operationUtils;

    public BoxInfoEntity verifySign(TokenInfo tokenInfo) {
        Optional<BoxInfoEntity> boxInfoEntityOp = boxInfoEntityRepository.findByBoxUUID(tokenInfo.getBoxUUID());
        if (boxInfoEntityOp.isPresent()) {
            if (AuthTypeEnum.BOX_UUID.getName().equals(boxInfoEntityOp.get().getAuthType())) {
                return boxInfoEntityOp.get();
            }
            if (CommonUtils.isNullOrEmpty(tokenInfo.getSign())) {
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "tokenInfo.sign");
            }
            var tokenVerifySignInfo = TokenVerifySignInfo.of(tokenInfo.getBoxUUID(), tokenInfo.getServiceIds());
            boolean verifySignInfo = operationUtils.verifySignUsingBoxPublicKey(operationUtils.objectToJson(tokenVerifySignInfo), tokenInfo.getSign(), boxInfoEntityOp.get().getBoxPubKey());

            if (verifySignInfo) {
                return boxInfoEntityOp.get();
            } else {
                LOG.errorv("failed to verify signature boxUUID :{0}", tokenInfo.getBoxUUID());
                throw new WebApplicationException("signature verification failed", Response.Status.FORBIDDEN);
            }
        } else {
            LOG.errorv("invalid boxUUID :{0}", tokenInfo.getBoxUUID());
            throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
        }
    }

    @Transactional
    public ArrayList<TokenResult> createBoxTokens(TokenInfo tokenInfo, BoxInfoEntity boxInfoEntity) {
        var result = new ArrayList<TokenResult>();

        tokenInfo.getServiceIds().forEach(serviceId -> {
            // 生成 token
            BoxTokenEntity boxTokenEntity = createBoxToken(tokenInfo.getBoxUUID(), ServiceEnum.fromValue(serviceId));
            result.add(TokenResult.of(boxTokenEntity.getServiceId(), boxTokenEntity.getBoxRegKey(), boxTokenEntity.getExpiresAt()));

        });
        return result;
    }

    @Transactional
    public BoxTokenEntity createBoxToken(String boxUUID, ServiceEnum serviceEnum) {
        return createBoxToken(boxUUID, serviceEnum, "brk_" + CommonUtils.createUnifiedRandomCharacters(10));
    }

    @Transactional
    public BoxTokenEntity createBoxToken(String boxUUID, ServiceEnum serviceEnum, String boxRegKey) {
        // 生成 token
        BoxTokenEntity boxTokenEntity = new BoxTokenEntity();
        {
            boxTokenEntity.setBoxUUID(boxUUID);
            boxTokenEntity.setServiceId(serviceEnum.getServiceId());
            boxTokenEntity.setServiceName(serviceEnum.name());
            boxTokenEntity.setBoxRegKey(boxRegKey);
            boxTokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(24));
        }
        boxTokenEntityRepository.persist(boxTokenEntity);
        return boxTokenEntity;
    }

    public BoxTokenEntity verifyRegistryBoxRegKey(String boxUUID, String boxRegKey) {
        return verifyBoxRegKey(boxUUID, boxRegKey, REGISTRY);
    }

    public void verifyOpsBoxRegKey(String boxUUID, String boxRegKey) {
        verifyBoxRegKey(boxUUID, boxRegKey, ServiceEnum.OPSTAGE);
    }

    public BoxTokenEntity verifyBoxRegKey(String boxUUID, String boxRegKey, ServiceEnum serviceEnum) {
        var boxTokenEntity = boxTokenEntityRepository.findByBoxRegKey(boxRegKey);
        if (boxTokenEntity.isEmpty()) {
            throw new WebApplicationException("invalid boxRegKey", Response.Status.UNAUTHORIZED);
        }
        if (!ServiceEnum.fromValue(boxTokenEntity.get().getServiceId()).equals(serviceEnum)) {
            throw new WebApplicationException("boxRegKey verification failed, service platform mismatch", Response.Status.UNAUTHORIZED);
        }
        if (!boxTokenEntity.get().getBoxUUID().equals(boxUUID)) {
            throw new WebApplicationException("insufficient permissions", Response.Status.UNAUTHORIZED);
        }
        if (boxTokenEntity.get().getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new WebApplicationException("boxRegKey expired", Response.Status.UNAUTHORIZED);
        }
        return boxTokenEntity.get();
    }
}
