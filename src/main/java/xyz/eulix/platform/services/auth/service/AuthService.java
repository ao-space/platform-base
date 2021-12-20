package xyz.eulix.platform.services.auth.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.entity.PkeyAuthEntity;
import xyz.eulix.platform.services.auth.repository.PkeyAuthEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger("app.log");

    // pkey有效时间
    private static final Integer PKEY_EXPIRE_TIME_MIN = 2;

    @Inject
    PkeyAuthEntityRepository pkeyAuthEntityRepository;

    /**
     * 将PkeyAuth存入中间件
     * @param boxInfoReq box info
     */
    @Transactional
    public void savePkeyAuth(TransBoxInfoReq boxInfoReq) {
        // 检验pkey有效性
        PkeyAuthEntity pkeyAuthEntity = getPkeyAuth(boxInfoReq.getPkey());
        if (pkeyAuthEntity == null) {
            LOG.warnv("pkey is invalid, pkey:{0}", boxInfoReq.getPkey());
            throw new ServiceOperationException(ServiceError.PKEY_INVALID);
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isAfter(pkeyAuthEntity.getExpiresAt())) {
            LOG.infov("pkey is expired, pkey:{0}", boxInfoReq.getPkey());
            throw new ServiceOperationException(ServiceError.PKEY_EXPIRED);
        }
        // 保存box info
        pkeyAuthEntityRepository.update("bkey=?1, user_domain=?2, box_pub_key=?3 WHERE pkey=?4",
                boxInfoReq.getBkey(), boxInfoReq.getUserDomain(), boxInfoReq.getBoxPubKey(), boxInfoReq.getPkey());
    }

    public PkeyAuthEntity getPkeyAuth(String pkey) {
        return pkeyAuthEntityRepository.findByPkey(pkey);
    }

    /**
     * 从中间件获取PkeyAuth
     * @param pkey pkey
     * @return PollPkeyRsp
     */
    public PollPkeyRsp pollPkeyAuth(String pkey) {
        PkeyAuthEntity pkeyAuthEntity = getPkeyAuth(pkey);
        if (pkeyAuthEntity == null) {
            LOG.warnv("pkey is invalid, pkey:{0}", pkey);
            throw new ServiceOperationException(ServiceError.PKEY_INVALID);
        }
        return pkeyAuthEntityToPollPkeyRsp(pkeyAuthEntity);
    }

    /**
     * 生成、保存pkey
     * @return PkeyAuthEntity
     */
    @Transactional
    public PkeyAuthEntity genPkey() {
        PkeyAuthEntity pkeyAuthEntity = new PkeyAuthEntity();
        // 生成pkey
        String uuid = UUID.randomUUID().toString();
        pkeyAuthEntity.setPkey(uuid);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(PKEY_EXPIRE_TIME_MIN);
        pkeyAuthEntity.setExpiresAt(expiresAt);
        // 保存pkey
        pkeyAuthEntityRepository.persist(pkeyAuthEntity);
        return pkeyAuthEntity;
    }

    public PollPkeyRsp pkeyAuthEntityToPollPkeyRsp(PkeyAuthEntity pkeyAuthEntity){
        return PollPkeyRsp.of(pkeyAuthEntity.getPkey(), pkeyAuthEntity.getBkey(), pkeyAuthEntity.getUserDomain(),
                pkeyAuthEntity.getBoxPubKey());
    }
}
