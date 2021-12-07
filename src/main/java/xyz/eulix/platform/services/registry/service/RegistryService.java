package xyz.eulix.platform.services.registry.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class RegistryService {
    private static final Logger LOG = Logger.getLogger("app.log");

    // 默认user/client id
    private static final String DEFAULT_ID = "0";
    // 默认user/client reg key
    private static final String DEFAULT_REG_KEY = "0";

    @Inject
    ApplicationProperties properties;

    @Inject
    RegistryEntityRepository registryRepository;

    private volatile boolean boxUUIDCheckByPass = false;
    private final Set<String> boxUUIDWhiteSet = Sets.newConcurrentHashSet();

    @PostConstruct
    void init() {
        final String policy = properties.getRegistryBoxUUIDPolicy().trim();
        if ("all".equals(policy)) {
            boxUUIDCheckByPass = true;
        } else if (policy.startsWith("list$")) {
            boxUUIDWhiteSet.addAll(Splitter.on(",").trimResults().splitToList(policy.substring(5)));
        } else {
            throw new IllegalArgumentException("not support yet!");
        }
    }

    public boolean isValidBoxUUID(String uuid) {
        if (boxUUIDCheckByPass) {
            return true;
        } else {
            return boxUUIDWhiteSet.contains(uuid);
        }
    }

    public boolean verifyClient(String clientRegKey, String boxUUID, String userId, String clientUUID) {
        List<RegistryEntity> clientEntities = findAllByClientUUIDAndClientRegKey(boxUUID, clientUUID, userId, clientRegKey);
        return !clientEntities.isEmpty();
    }

    public boolean verifyUser(String userRegKey, String boxUUID, String userId) {
        List<RegistryEntity> userEntities = findAllByUserIDAndUserRegKey(boxUUID, userId, userRegKey);
        return !userEntities.isEmpty();
    }

    public boolean verifyBox(String boxRegKey, String boxUUID) {
        Optional<RegistryEntity> boxEntityOp = findByBoxUUIDAndBoxRegKeyAndType(boxUUID, boxRegKey);
        return boxEntityOp.isPresent();
    }

    @Transactional
    public void deleteByBoxUUID(String boxUUID) {
        registryRepository.delete("box_uuid", boxUUID);
    }

    @Transactional
    public void deleteByUserId(String boxUUID, String userId) {
        registryRepository.deleteByUserId(boxUUID, userId);
    }

    @Transactional
    public void deleteByClientUUID(String boxUUID, String userId, String clientUUID) {
        registryRepository.deleteByClientUUID(boxUUID, userId, clientUUID);
    }

    public Optional<RegistryEntity> findByBoxUUIDAndType(String boxUUID) {
        return registryRepository.findByBoxUUIDAndType(boxUUID, RegistryTypeEnum.BOX.getName()).singleResultOptional();
    }

    public Optional<RegistryEntity> findByBoxUUIDAndBoxRegKeyAndType(String boxUUID, String boxRegKey) {
        return registryRepository.findByBoxUUIDAndBoxRegKeyAndType(boxUUID, boxRegKey, RegistryTypeEnum.BOX.getName()).singleResultOptional();
    }

    public List<RegistryEntity> findAllByUserId(String boxUUID, String userId) {
        return registryRepository.findAllByUserId(boxUUID, userId);
    }

    public List<RegistryEntity> findAllByUserIDAndUserRegKey(String boxUUID, String userId, String userRegKey) {
        return registryRepository.findAllByUserIDAndUserRegKey(boxUUID, userId, userRegKey);
    }

    private List<RegistryEntity> findAllByClientUUID(String boxUUID, String userId, String clientUUID) {
        return registryRepository.findAllByClientUUID(boxUUID, userId, clientUUID);
    }

    public List<RegistryEntity> findAllByClientUUIDAndClientRegKey(String boxUUID, String clientUUID, String userId, String clientRegKey) {
        return registryRepository.findAllByClientUUIDAndClientRegKey(boxUUID, userId, clientUUID, clientRegKey);
    }

    public Optional<RegistryEntity> findByUserDomain(String userDomain) {
        return registryRepository.findByUserDomain(userDomain);
    }

    @Transactional
    public RegistryResult registryBox(RegistryInfo info) {
        // 注册box
        String boxDomain = getUserDomain(null);   // 兼容network proxy缺失
        RegistryEntity boxEntity = registryBox(info.getBoxUUID(), boxDomain);

        // 注册用户（管理员）
        String userDomain = getUserDomain(info.getSubdomain());
        RegistryEntity userEntity = registryUser(boxEntity.getBoxUUID(), boxEntity.getBoxRegKey(), info.getUserId(), userDomain,
                RegistryTypeEnum.USER_ADMIN);

        // 注册client（管理员的绑定设备）
        RegistryEntity clientEntity = registryClient(boxEntity.getBoxUUID(), boxEntity.getBoxRegKey(), userEntity.getUserId(),
                userEntity.getUserRegKey(), info.getClientUUID(), RegistryTypeEnum.CLIENT_BIND);

        // 计算路由

        NetworkClient networkClient = NetworkClient.of(boxEntity.getNetworkClientId(), boxEntity.getNetworkSecretKey());
        return RegistryResult.of(boxEntity.getBoxRegKey(), boxEntity.getUserDomain(), userEntity.getUserRegKey(), clientEntity.getClientRegKey(),
                networkClient);
    }

    @Transactional
    public UserRegistryResult registryUser(UserRegistryInfo userRegistryInfo) {
        // 注册用户
        String userDomain = getUserDomain(userRegistryInfo.getSubdomain());
        RegistryEntity userEntity = registryUser(userRegistryInfo.getBoxUUID(), userRegistryInfo.getBoxRegKey(), userRegistryInfo.getUserId(),
                userDomain, RegistryTypeEnum.fromValue(userRegistryInfo.getUserType()));

        // 注册client（用户的绑定设备）
        RegistryEntity clientEntity = registryClient(userRegistryInfo.getBoxUUID(), userRegistryInfo.getBoxRegKey(), userEntity.getUserId(),
                userEntity.getUserRegKey(), userRegistryInfo.getClientUUID(), RegistryTypeEnum.CLIENT_BIND);

        return UserRegistryResult.of(userDomain, userEntity.getUserRegKey(), clientEntity.getClientRegKey());
    }

    @Transactional
    public RegistryEntity registryBox(String boxUUID, String boxDomain) {
        // 注册box
        RegistryEntity boxEntity = new RegistryEntity();
        {
            boxEntity.setBoxUUID(boxUUID);
            boxEntity.setBoxRegKey("brk_" + CommonUtils.createUnifiedRandomCharacters(10));
            boxEntity.setUserDomain(boxDomain);    // 兼容network proxy缺失
            boxEntity.setRegistryType(RegistryTypeEnum.BOX.getName());
            // network client
            boxEntity.setNetworkClientId(boxDomain);    // 兼容network proxy缺失
            boxEntity.setNetworkSecretKey("nrk_" + CommonUtils.createUnifiedRandomCharacters(10));
        }
        registryRepository.persist(boxEntity);
        return boxEntity;
    }

    @Transactional
    public RegistryEntity registryUser(String boxUUID, String boxRegKey, String userId, String userDomain, RegistryTypeEnum userType) {
        RegistryEntity userEntity = new RegistryEntity();
        {
            userEntity.setBoxUUID(boxUUID);
            userEntity.setBoxRegKey(boxRegKey);
            userEntity.setUserId(userId);
            userEntity.setUserRegKey("urk_" + CommonUtils.createUnifiedRandomCharacters(10));
            userEntity.setUserDomain(userDomain);
            userEntity.setRegistryType(userType.getName());
        }
        registryRepository.persist(userEntity);
        return userEntity;
    }

    @Transactional
    public RegistryEntity registryClient(String boxUUID, String boxRegKey, String userId, String userRegKey, String clientUUID,
                                         RegistryTypeEnum clientType) {
        RegistryEntity clientEntity = new RegistryEntity();
        {
            clientEntity.setBoxUUID(boxUUID);
            clientEntity.setBoxRegKey(boxRegKey);
            clientEntity.setUserId(userId);
            clientEntity.setUserRegKey(userRegKey);
            clientEntity.setClientUUID(clientUUID);
            clientEntity.setClientRegKey("crk_" + CommonUtils.createUnifiedRandomCharacters(10));
            clientEntity.setRegistryType(clientType.getName());
        }
        registryRepository.persist(clientEntity);
        return clientEntity;
    }

    @Transactional
    public RegistryEntity createClientRegistry(RegistryEntity boxRegistryEntity, String clientUUID, String userDomain) {
        RegistryEntity entity = new RegistryEntity();
        {
            entity.setBoxRegKey(boxRegistryEntity.getBoxRegKey());
            entity.setClientRegKey("crk_" + CommonUtils.createUnifiedRandomCharacters(10));
            entity.setBoxUUID(boxRegistryEntity.getBoxUUID());
            entity.setClientUUID(clientUUID);
            entity.setUserDomain(userDomain);
            entity.setRegistryType(RegistryTypeEnum.CLIENT_BIND.getName());  //todo
        }
        registryRepository.persist(entity);
        return entity;
    }


    /**
     * 获取 userDomain
     * 1.如果subDomain不为空，直接使用
     * 2.如果subDomain为空，自动生成长度8位的subdomain
     *
     * @param subDomain subDomain
     * @return 用户域名
     */
    public String getUserDomain(String subDomain) {
        if (CommonUtils.isNullOrEmpty(subDomain)) {
            subDomain = CommonUtils.randomLetters(1) + CommonUtils.randomDigestAndLetters(7);
        }
        return subDomain + "." + properties.getRegistrySubdomain();
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
        final Optional<RegistryEntity> boxEntityOp = findByBoxUUIDAndType(boxUUID);
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
    public void hasBoxNotRegistered(String boxUUID, String boxRegKey) {
        final Optional<RegistryEntity> boxEntityOp = findByBoxUUIDAndBoxRegKeyAndType(boxUUID, boxRegKey);
        if (boxEntityOp.isEmpty()) {
            LOG.warnv("invalid box registry info, boxUuid:{0}", boxUUID);
            throw new WebApplicationException("invalid box registry info.", Response.Status.FORBIDDEN);
        }
    }

    /**
     * 校验subdomain是否已存在
     *
     * @param subdomain subdomain
     */
    public void isSubdomainExist(String subdomain) {
        Optional<RegistryEntity> reOp = findByUserDomain(subdomain + "." + properties.getRegistrySubdomain());
        if (reOp.isPresent()) {
            LOG.warnv("subdomain already exist, subdomain:{0}", subdomain);
            throw new ServiceOperationException(ServiceError.SUBDOMAIN_ALREADY_EXIST);
        }
    }

    /**
     * 校验用户是否已注册
     *
     * @param boxUUID boxUUID
     * @param userId userId
     */
    public void hasUserRegistered(String boxUUID, String userId) {
        final List<RegistryEntity> userEntitys = findAllByUserId(boxUUID, userId);
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
        final List<RegistryEntity> userEntitys = findAllByUserIDAndUserRegKey(boxUUID, userId, userRegKey);
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
        final List<RegistryEntity> clientEntitys = findAllByClientUUID(boxUUID, userId, clientUUID);
        throw new WebApplicationException("client uuid had already registered. Pls reset and try again.", Response.Status.NOT_ACCEPTABLE);
    }
}
