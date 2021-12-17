package xyz.eulix.platform.services.registry.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.network.service.NetworkService;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.entity.RegistryClientEntity;
import xyz.eulix.platform.services.registry.entity.RegistryUserEntity;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.RegistryBoxEntityRepository;
import xyz.eulix.platform.services.registry.repository.RegistryClientEntityRepository;
import xyz.eulix.platform.services.registry.repository.RegistryUserEntityRepository;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class RegistryService {
    private static final Logger LOG = Logger.getLogger("app.log");

    // 盒子可申请的subdomain数量上限
    private static final Integer SUBDOMAIN_UPPER_LIMIT = 1000;

    @Inject
    ApplicationProperties properties;

    @Inject
    RegistryBoxEntityRepository boxEntityRepository;

    @Inject
    RegistryUserEntityRepository userEntityRepository;

    @Inject
    RegistryClientEntityRepository clientEntityRepository;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Inject
    NetworkService networkService;

    @Inject
    BoxInfoService boxInfoService;

    public boolean isValidBoxUUID(String boxUUID) {
        final String policy = properties.getRegistryBoxUUIDPolicy().trim();
        if ("all".equals(policy)) {
            return true;
        } else {
            return boxInfoService.isValidBoxUUID(boxUUID);
        }
    }

    public boolean verifyClient(String clientRegKey, String boxUUID, String userId, String clientUUID) {
        List<RegistryClientEntity> clientEntities = clientEntityRepository.findAllByClientUUIDAndClientRegKey(boxUUID, userId, clientUUID, clientRegKey);
        return !clientEntities.isEmpty();
    }

    public boolean verifyUser(String userRegKey, String boxUUID, String userId) {
        List<RegistryUserEntity> userEntities = userEntityRepository.findAllByUserIDAndUserRegKey(boxUUID, userId, userRegKey);
        return !userEntities.isEmpty();
    }

    public boolean verifyBox(String boxRegKey, String boxUUID) {
        Optional<RegistryBoxEntity> boxEntityOp = boxEntityRepository.findByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
        return boxEntityOp.isPresent();
    }

    @Transactional
    public void deleteBoxByBoxUUID(String boxUUID) {
        boxEntityRepository.delete("box_uuid", boxUUID);
    }

    @Transactional
    public void deleteUserByUserId(String boxUUID, String userId) {
        userEntityRepository.deleteByUserId(boxUUID, userId);
    }

    @Transactional
    public void deleteUserByBoxUUID(String boxUUID) {
        userEntityRepository.deleteByBoxUUID(boxUUID);
    }

    @Transactional
    public void deleteClientByUserId(String boxUUID, String userId) {
        clientEntityRepository.deleteByUserId(boxUUID, userId);
    }

    @Transactional
    public void deleteClientByBoxUUID(String boxUUID) {
        clientEntityRepository.deleteByBoxUUID(boxUUID);
    }

    @Transactional
    public void deleteClientByClientUUID(String boxUUID, String userId, String clientUUID) {
        clientEntityRepository.deleteByClientUUID(boxUUID, userId, clientUUID);
    }

    @Transactional
    public void deleteSubdomainByUserId(String boxUUID, String userId) {
        subdomainEntityRepository.deleteSubdomainByUserId(boxUUID, userId);
    }

    @Transactional
    private void deleteSubdomainByBoxUUID(String boxUUID) {
        subdomainEntityRepository.deleteSubdomainByBoxUUID(boxUUID);
    }

    @Transactional
    public BoxRegistryResult registryBox(BoxRegistryInfo info) {
        // 申请subdomain，兼容network proxy缺失
        SubdomainEntity subdomainEntity = subdomainGen(info.getBoxUUID());
        // 注册box
        RegistryBoxEntity boxEntity = registryBox(info.getBoxUUID(), subdomainEntity.getSubdomain());
        // 计算路由
        networkService.calculateNetworkRoute(boxEntity.getNetworkClientId());
        return BoxRegistryResult.of(boxEntity.getBoxRegKey(), NetworkClient.of(boxEntity.getNetworkClientId(), boxEntity.getNetworkSecretKey()));
    }

    @Transactional
    public UserRegistryResult registryUser(UserRegistryInfo userRegistryInfo, String userDomain) {
        // 注册用户
        RegistryUserEntity userEntity = registryUser(userRegistryInfo.getBoxUUID(), userRegistryInfo.getUserId(),
                RegistryTypeEnum.fromValue(userRegistryInfo.getUserType()));

        // 修改域名状态
        subdomainEntityRepository.updateBySubdomain(userRegistryInfo.getUserId(), SubdomainStateEnum.USED.getState(),
                userRegistryInfo.getSubdomain());

        // 注册client（用户的绑定设备）
        RegistryClientEntity clientEntity = registryClient(userRegistryInfo.getBoxUUID(), userEntity.getUserId(),userRegistryInfo.getClientUUID(),
                RegistryTypeEnum.CLIENT_BIND);

        return UserRegistryResult.of(userDomain, userEntity.getUserRegKey(), clientEntity.getClientRegKey());
    }

    @Transactional
    public RegistryBoxEntity registryBox(String boxUUID, String subdomain) {
        // 注册box
        RegistryBoxEntity boxEntity = new RegistryBoxEntity();
        {
            boxEntity.setBoxUUID(boxUUID);
            boxEntity.setBoxRegKey("brk_" + CommonUtils.createUnifiedRandomCharacters(10));
            // network client
            boxEntity.setNetworkClientId(subdomain);    // 兼容network proxy缺失
            boxEntity.setNetworkSecretKey("nrk_" + CommonUtils.createUnifiedRandomCharacters(10));
        }
        boxEntityRepository.persist(boxEntity);
        return boxEntity;
    }

    @Transactional
    public RegistryUserEntity registryUser(String boxUUID, String userId, RegistryTypeEnum userType) {
        RegistryUserEntity userEntity = new RegistryUserEntity();
        {
            userEntity.setBoxUUID(boxUUID);
            userEntity.setUserId(userId);
            userEntity.setUserRegKey("urk_" + CommonUtils.createUnifiedRandomCharacters(10));
            userEntity.setRegistryType(userType.getName());
        }
        userEntityRepository.persist(userEntity);
        return userEntity;
    }

    @Transactional
    public RegistryClientEntity registryClient(String boxUUID, String userId, String clientUUID, RegistryTypeEnum clientType) {
        RegistryClientEntity clientEntity = new RegistryClientEntity();
        {
            clientEntity.setBoxUUID(boxUUID);
            clientEntity.setUserId(userId);
            clientEntity.setClientUUID(clientUUID);
            clientEntity.setClientRegKey("crk_" + CommonUtils.createUnifiedRandomCharacters(10));
            clientEntity.setRegistryType(clientType.getName());
        }
        clientEntityRepository.persist(clientEntity);
        return clientEntity;
    }


    @Transactional
    public void resetUser(UserRegistryResetInfo userResetInfo) {
        // 重置用户
        deleteUserByUserId(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        // 重置域名
        deleteSubdomainByUserId(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
        // 重置client
        deleteClientByUserId(userResetInfo.getBoxUUID(), userResetInfo.getUserId());
    }

    @Transactional
    public void resetBox(BoxRegistryResetInfo boxResetInfo) {
        // 重置盒子
        deleteBoxByBoxUUID(boxResetInfo.getBoxUUID());
        // 重置用户
        deleteUserByBoxUUID(boxResetInfo.getBoxUUID());
        // 重置域名
        deleteSubdomainByBoxUUID(boxResetInfo.getBoxUUID());
        // 重置client
        deleteClientByBoxUUID(boxResetInfo.getBoxUUID());
    }

    /**
     * 校验boxUUID合法性
     *
     * @param boxUUID boxUUID
     */
    public void isValidBoxUUIDThrowEx(String boxUUID) {
        final boolean validBoxUUID = isValidBoxUUID(boxUUID);
        if (!validBoxUUID) {
            throw new WebApplicationException("invalid box uuid", Response.Status.FORBIDDEN);
        }
    }

    /**
     * 校验盒子是否已注册
     *
     * @param boxUUID boxUUID
     */
    public void hasBoxRegistered(String boxUUID) {
        final Optional<RegistryBoxEntity> boxEntityOp = boxEntityRepository.findByBoxUUID(boxUUID);
        if (boxEntityOp.isPresent()) {
            LOG.warnv("box uuid had already registered, boxuuid:{0}", boxUUID);
            throw new WebApplicationException("box uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
        }
    }

    /**
     * 校验盒子是否未注册
     *
     * @param boxUUID boxUUID
     * @param boxRegKey 盒子的注册码
     */
    public RegistryBoxEntity hasBoxNotRegistered(String boxUUID, String boxRegKey) {
        final Optional<RegistryBoxEntity> boxEntityOp = boxEntityRepository.findByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
        if (boxEntityOp.isEmpty()) {
            LOG.warnv("invalid box registry info, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid box registry info.", Response.Status.FORBIDDEN);
        }
        return boxEntityOp.get();
    }

    /**
     * 校验subdomain是否不存在，或者已使用
     *
     * @param subdomain subdomain
     */
    public SubdomainEntity isSubdomainNotExistOrUsed(String subdomain) {
        Optional<SubdomainEntity> subdomainEntityOp = subdomainEntityRepository.findBySubdomain(subdomain);
        if (subdomainEntityOp.isEmpty()) {
            LOG.warnv("subdomain does not exist, subdomain:{0}", subdomain);
            throw new ServiceOperationException(ServiceError.SUBDOMAIN_NOT_EXIST);
        }
        if (SubdomainStateEnum.USED.getState().equals(subdomainEntityOp.get().getState())) {
            LOG.warnv("subdomain already used, subdomain:{0}", subdomain);
            throw new ServiceOperationException(ServiceError.SUBDOMAIN_ALREADY_USED);
        }
        return subdomainEntityOp.get();
    }

    /**
     * 校验用户是否已注册
     *
     * @param boxUUID boxUUID
     * @param userId userId
     */
    public void hasUserRegistered(String boxUUID, String userId) {
        final List<RegistryUserEntity> userEntitys = userEntityRepository.findAllByUserId(boxUUID, userId);
        if (!userEntitys.isEmpty()) {
            LOG.warnv("user id had already registered, boxUUID:{0}, userId:{1}", boxUUID, userId);
            throw new WebApplicationException("user id had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
        }
    }

    /**
     * 校验用户是否未注册
     *
     * @param boxUUID boxUUID
     * @param userId userId
     * @param userRegKey 用户的注册码
     */
    public void hasUserNotRegistered(String boxUUID, String userId, String userRegKey) {
        final List<RegistryUserEntity> userEntitys = userEntityRepository.findAllByUserIDAndUserRegKey(boxUUID, userId, userRegKey);
        if (userEntitys.isEmpty()) {
            LOG.warnv("invalid user registry info, boxUUID:{0}, userId:{1}", boxUUID, userId);
            throw new WebApplicationException("invalid user registry info.", Response.Status.FORBIDDEN);
        }
    }

    /**
     * 校验client是否已注册
     *
     * @param boxUUID boxUUID
     * @param userId userId
     * @param clientUUID clientUUID
     */
    public void hasClientRegistered(String boxUUID, String userId, String clientUUID) {
        final List<RegistryClientEntity> clientEntitys = clientEntityRepository.findAllByClientUUID(boxUUID, userId, clientUUID);
        if (!clientEntitys.isEmpty()) {
            LOG.warnv("client uuid had already registered, boxUUID:{0}, userId:{1}, clientUUID:{2}", boxUUID, userId, clientUUID);
            throw new WebApplicationException("client uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
        }
    }

    /**
     * 分发全局唯一的 subdomain，无超时时间
     *
     * @param boxUUID boxUUID
     * @return subdomain
     */
    public SubdomainEntity subdomainGen(String boxUUID) {
        return subdomainGen(boxUUID, null);
    }

    /**
     * 分发全局唯一的 subdomain
     *
     * @param boxUUID boxUUID
     * @param effectiveTime 有效期，单位秒
     * @return subdomainEntity
     */
    public SubdomainEntity subdomainGen(String boxUUID, Integer effectiveTime) {
        // 生成临时subdomain
        SubdomainEntity subdomainEntity;
        while (true) {
            String subdomain = null;
            try {
                subdomain = CommonUtils.randomLetters(1) + CommonUtils.randomDigestAndLetters(7);
                subdomainEntity = subdomainSave(boxUUID, subdomain, effectiveTime);
                break;
            } catch (PersistenceException exception) {
                if (exception.getCause() != null && exception.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
                    LOG.infov("subdomain:{0} already exists, retry...", subdomain);
                } else {
                    LOG.errorv(exception, "subdomain save failed");
                    throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
                }
            }
        }

        return subdomainEntity;
    }

    @Transactional
    public SubdomainEntity subdomainSave(String boxUUID, String subdomain, Integer effectiveTime) {
        SubdomainEntity subdomainEntity = new SubdomainEntity();
        {
            subdomainEntity.setBoxUUID(boxUUID);
            subdomainEntity.setSubdomain(subdomain);
            subdomainEntity.setUserDomain(subdomain + "." + properties.getRegistrySubdomain());
            if (effectiveTime != null) {
                subdomainEntity.setExpiresAt(OffsetDateTime.now().plusSeconds(effectiveTime));
            }
            subdomainEntity.setState(SubdomainStateEnum.TEMPORARY.getState());
        }
        subdomainEntityRepository.persist(subdomainEntity);
        return  subdomainEntity;
    }

    public void reachUpperLimit(String boxUUID) {
        Long count = subdomainEntityRepository.count("box_uuid", boxUUID);
        if (count > SUBDOMAIN_UPPER_LIMIT) {
            LOG.warnv("reach subdomain upper limit, boxUUID:{0}", boxUUID);
            throw new ServiceOperationException(ServiceError.SUBDOMAIN_UPPER_LIMIT);
        }
    }

    public Boolean networkClientAuth(String clientId, String secretKey) {
        Optional<RegistryBoxEntity> registryBoxEntityOp = boxEntityRepository.findByClientIdAndSecretKey(clientId, secretKey);
        if (registryBoxEntityOp.isEmpty()) {
            LOG.infov("network client auth failed, client id:{0}, secret key:{1}", clientId, secretKey);
        }
        return registryBoxEntityOp.isPresent();
    }
}
