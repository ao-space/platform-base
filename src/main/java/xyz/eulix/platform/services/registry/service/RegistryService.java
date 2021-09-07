package xyz.eulix.platform.services.registry.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.RegistryInfo;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;
import xyz.eulix.platform.services.support.Utils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class RegistryService {

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

  @Transactional
  public boolean verifyClient(String clientRegKey, String clientUUID) {
    Optional<RegistryEntity> rp = registryRepository.find(
        "client_uuid", clientUUID).singleResultOptional();
    return rp.filter(r -> clientRegKey.equals(r.getClientRegKey())).isPresent();
  }

  @Transactional
  public boolean verifyBox(String boxRegKey, String boxUUID) {
    Optional<RegistryEntity> rp = registryRepository.find(
        "box_uuid", boxUUID).singleResultOptional();
    return rp.filter(r -> boxRegKey.equals(r.getBoxRegKey())).isPresent();
  }

  @Transactional
  public void deleteByBoxRegKey(String key) {
    registryRepository.delete("box_reg_key", key);
  }

  @Transactional
  public Optional<RegistryEntity> findByBoxUUID(String uuid) {
    return registryRepository.find("box_uuid", uuid).singleResultOptional();
  }

  @Transactional
  public RegistryEntity createRegistry(RegistryInfo info) {
    RegistryEntity entity = new RegistryEntity();
    {
      entity.setBoxRegKey("brk_" + Utils.createUnifiedRandomCharacters(10));
      entity.setClientRegKey("crk_" + Utils.createUnifiedRandomCharacters(10));
      entity.setBoxUUID(info.getBoxUUID());
      entity.setClientUUID(info.getClientUUID());
      entity.setSubdomain(info.getSubdomain() + "." + properties.getRegistrySubdomain());
    }
    registryRepository.persist(entity);
    return entity;
  }

}
