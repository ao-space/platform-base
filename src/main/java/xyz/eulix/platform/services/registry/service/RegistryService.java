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
    return registryRepository.find("box_uuid", uuid).singleResultOptional();
  }

  public List<RegistryEntity> findAllByClientUUIDAndClientRegKey(String boxUUID, String clientUUID, String clientRegKey) {
    return registryRepository.findAllByClientUUIDAndClientRegKey(boxUUID, clientUUID, clientRegKey);
  }

  public List<RegistryEntity> findAllByBoxUUIDAndBoxRegKey(String boxUUID, String boxRegKey) {
    return registryRepository.findAllByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
  }

  @Transactional
  public RegistryEntity createRegistry(RegistryInfo info) {
    RegistryEntity entity = new RegistryEntity();
    {
      entity.setBoxRegKey("brk_" + CommonUtils.createUnifiedRandomCharacters(10));
      entity.setClientRegKey(DEFAULT_CLIENT_REG_KEY);
      entity.setBoxUUID(info.getBoxUUID());
      entity.setClientUUID(DEFAULT_CLIENT_UUID);
      entity.setSubdomain(info.getSubdomain() + "." + properties.getRegistrySubdomain());
      entity.setRegistryType(RegistryTypeEnum.BOX.getName());
    }
    registryRepository.persist(entity);
    return entity;
  }

  @Transactional
  public RegistryEntity createClientRegistry(RegistryEntity boxRegistryEntity, String clientUUID) {
    RegistryEntity entity = new RegistryEntity();
    {
      entity.setBoxRegKey(boxRegistryEntity.getBoxRegKey());
      entity.setClientRegKey("crk_" + CommonUtils.createUnifiedRandomCharacters(10));
      entity.setBoxUUID(boxRegistryEntity.getBoxUUID());
      entity.setClientUUID(clientUUID);
      entity.setSubdomain(boxRegistryEntity.getSubdomain());
      entity.setRegistryType(RegistryTypeEnum.CLIENT.getName());
    }
    registryRepository.persist(entity);
    return entity;
  }

}
