package xyz.eulix.platform.services.auth.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.entity.BoxInfoEntity;
import xyz.eulix.platform.services.auth.repository.BoxInfoEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger("app.log");

    // pkey有效时间
    private static final Integer PKEY_EXPIRE_TIME_MIN = 2;

    @Inject
    BoxInfoEntityRepository boxInfoEntityRepository;

    /**
     * 将box info存入中间件
     * @param boxInfoReq box info
     */
    @Transactional
    public void saveBoxInfo(TransBoxInfoReq boxInfoReq) {
        // 检验pkey有效性
        BoxInfoEntity boxInfoEntity = getBoxInfo(boxInfoReq.getPkey());
        if (boxInfoEntity == null) {
            LOG.warnv("pkey is invalid, pkey:{0}", boxInfoReq.getPkey());
            throw new ServiceOperationException(ServiceError.PKEY_INVALID);
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isAfter(boxInfoEntity.getExpiresAt())) {
            LOG.infov("pkey is expired, pkey:{0}", boxInfoReq.getPkey());
            throw new ServiceOperationException(ServiceError.PKEY_EXPIRED);
        }
        // 保存box info
        boxInfoEntityRepository.update("bkey=?1, box_domain=?2, box_pub_key=?3 WHERE pkey=?4",
                boxInfoReq.getBkey(), boxInfoReq.getBoxDomain(), boxInfoReq.getBoxPubKey(), boxInfoReq.getPkey());
    }

    public BoxInfoEntity getBoxInfo(String pkey) {
        return boxInfoEntityRepository.findByPkey(pkey);
    }

    /**
     * 从中间件获取box info
     * @param pkey pkey
     * @return PollPkeyRsp
     */
    public PollPkeyRsp pollBoxInfo(String pkey) {
        BoxInfoEntity boxInfoEntity = getBoxInfo(pkey);
        if (boxInfoEntity == null) {
            LOG.warnv("pkey is invalid, pkey:{0}", pkey);
            throw new ServiceOperationException(ServiceError.PKEY_INVALID);
        }
        return boxInfoEntityToPollPkeyRsp(boxInfoEntity);
    }

    /**
     * 生成、保存pkey
     * @return BoxInfoEntity
     */
    @Transactional
    public BoxInfoEntity genPkey() {
        BoxInfoEntity boxInfoEntity = new BoxInfoEntity();
        // 生成pkey
        String uuid = UUID.randomUUID().toString();
        boxInfoEntity.setPkey(uuid);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(PKEY_EXPIRE_TIME_MIN);
        boxInfoEntity.setExpiresAt(expiresAt);
        // 保存pkey
        boxInfoEntityRepository.persist(boxInfoEntity);
        return boxInfoEntity;
    }

    public PollPkeyRsp boxInfoEntityToPollPkeyRsp(BoxInfoEntity boxInfoEntity){
        return PollPkeyRsp.of(boxInfoEntity.getPkey(), boxInfoEntity.getBkey(), boxInfoEntity.getBoxDomain(),
                boxInfoEntity.getBoxPubKey());
    }
}
