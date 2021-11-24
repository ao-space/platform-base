package xyz.eulix.platform.services.registry.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.RegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.RegistryTypeEnum;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class RegistryService {
  // 默认client uuid
  private static final String DEFAULT_CLIENT_UUID = "0";
  // 默认client reg key
  private static final String DEFAULT_CLIENT_REG_KEY = "0";

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

  public boolean verifyClient(String clientRegKey, String boxUUID, String clientUUID) {
    List<RegistryEntity> registryEntities = findAllByClientUUIDAndClientRegKey(boxUUID, clientUUID, clientRegKey);
    return !registryEntities.isEmpty();
  }

  public boolean verifyBox(String boxRegKey, String boxUUID) {
    List<RegistryEntity> registryEntities = findAllByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
    return !registryEntities.isEmpty();
  }

  @Transactional
  public void deleteByBoxUUID(String boxUUID) {
    registryRepository.delete("box_uuid", boxUUID);
  }

  @Transactional
  public void deleteByClientUUID(String boxUUID, String clientUUID) {
    registryRepository.deleteByClientUUID(boxUUID, clientUUID);
  }

  @Transactional
  public Optional<RegistryEntity> findByBoxUUID(String uuid) {
    return registryRepository.findByBoxUUIDAndType(uuid, RegistryTypeEnum.BOX.getName()).singleResultOptional();
  }

  public List<RegistryEntity> findAllByClientUUIDAndClientRegKey(String boxUUID, String clientUUID, String clientRegKey) {
    return registryRepository.findAllByClientUUIDAndClientRegKey(boxUUID, clientUUID, clientRegKey);
  }

  public List<RegistryEntity> findAllByBoxUUIDAndBoxRegKey(String boxUUID, String boxRegKey) {
    return registryRepository.findAllByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
  }

  public Optional<RegistryEntity> findByUserDomain(String userDomain) {
    return registryRepository.findByUserDomain(userDomain);
  }

  @Transactional
  public RegistryEntity createRegistry(RegistryInfo info, String clientUUID, String userDomain) {
    RegistryEntity entity = new RegistryEntity();
    {
      entity.setBoxRegKey("brk_" + CommonUtils.createUnifiedRandomCharacters(10));
      entity.setClientRegKey(DEFAULT_CLIENT_REG_KEY);
      entity.setBoxUUID(info.getBoxUUID());
      entity.setClientUUID(DEFAULT_CLIENT_UUID);
      entity.setUserDomain(null);
      entity.setRegistryType(RegistryTypeEnum.BOX.getName());
    }
    registryRepository.persist(entity);
    // 管理员 client 注册
    RegistryEntity reClient = createClientRegistry(entity, clientUUID, userDomain);
    return reClient;
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
      entity.setRegistryType(RegistryTypeEnum.CLIENT.getName());
    }
    registryRepository.persist(entity);
    return entity;
  }


  /**
   * 获取 userDomain
   * 1.如果subDomain不为空，直接使用
   * 2.如果subDomain为空，自动生成长度32位的subdomain，并保证未使用
   *
   * @param subDomain subDomain
   * @return 用户域名
   */
  public String getUserDomain(String subDomain) {
    String userDomain = null;
    if (CommonUtils.isNullOrEmpty(subDomain)) {
      for (int i=0; i<10; i++) {
        // 生成长度32位的subdomain
        userDomain = UUID.randomUUID().toString().replaceAll("-", "") + "." + properties.getRegistrySubdomain();
        // 校验是否使用
        Optional<RegistryEntity> registryEntityOp =  registryRepository.findByUserDomain(userDomain);
        if (registryEntityOp.isEmpty()) {
          return userDomain;
        }
      }
    } else {
      userDomain = subDomain + "." + properties.getRegistrySubdomain();
    }
    return userDomain;
  }
}
