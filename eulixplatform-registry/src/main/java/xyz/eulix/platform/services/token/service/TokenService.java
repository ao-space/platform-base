package xyz.eulix.platform.services.token.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
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

    public BoxInfoEntity verifySign(TokenInfo tokenInfo){
        var boxInfoEntity = boxInfoEntityRepository.findByBoxUUID(tokenInfo.getBoxUUID());
        if(boxInfoEntity.isPresent()){
            if(!boxInfoEntity.get().getAuthType().equals(AuthTypeEnum.BOX_PUB_KEY.getName())){
                return null;
            }
            var verifySignInfoJson = operationUtils.decryptUsingPublicKey(tokenInfo.getSign(), boxInfoEntity.get().getBoxPubKey());
            TokenVerifySignInfo verifySignInfo = operationUtils.jsonToObject(verifySignInfoJson, TokenVerifySignInfo.class);
            if(Objects.equals(verifySignInfo,
                    TokenVerifySignInfo.of(tokenInfo.getBoxUUID(), tokenInfo.getServiceIds()))){
                return boxInfoEntity.get();
            } else {
                throw new WebApplicationException("failed to verify signature", Response.Status.FORBIDDEN);
            }
        }
        throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
    }

    @Transactional
    public ArrayList<TokenResult> createBoxToken(TokenInfo tokenInfo, BoxInfoEntity boxInfoEntity){
        var result = new ArrayList<TokenResult>();

        tokenInfo.getServiceIds().forEach(serviceId -> {
            // 生成 token
            BoxTokenEntity boxTokenEntity = new BoxTokenEntity();
            {
                boxTokenEntity.setBoxUUID(tokenInfo.getBoxUUID());
                boxTokenEntity.setServiceId(serviceId);
                boxTokenEntity.setServiceName(ServiceEnum.fromValue(serviceId).name());
                boxTokenEntity.setBoxRegKey("brk_" + CommonUtils.createUnifiedRandomCharacters(10));
                boxTokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(24));
            }
            boxTokenEntityRepository.persist(boxTokenEntity);
            if(Objects.isNull(boxInfoEntity)){
                result.add(TokenResult.of(boxTokenEntity.getServiceId(), boxTokenEntity.getBoxRegKey(),
                    boxTokenEntity.getExpiresAt()));
            } else {
                result.add(TokenResult.of(boxTokenEntity.getServiceId(),
                    operationUtils.encryptUsingPublicKey(boxTokenEntity.getBoxRegKey(), boxInfoEntity.getBoxPubKey()),
                    boxTokenEntity.getExpiresAt()));
            }

        });
        return result;
    }

    public BoxTokenEntity verifyBoxRegKey(String boxUUID, String boxRegKey){
        var boxTokenEntity = boxTokenEntityRepository.findByBoxRegKey(boxRegKey);
        if(boxTokenEntity.isEmpty()){
            throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
        }
        if(!boxTokenEntity.get().getBoxUUID().equals(boxUUID)){
            throw new WebApplicationException("boxRegKey error", Response.Status.UNAUTHORIZED);
        }
        if(boxTokenEntity.get().getExpiresAt().isAfter(OffsetDateTime.now())){
            throw new WebApplicationException("boxRegKey expired", Response.Status.UNAUTHORIZED);
        }
        return boxTokenEntity.get();
    }
}
