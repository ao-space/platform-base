package xyz.eulix.platform.services.registry.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.registry.dto.registry.RegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.TunnelServer;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

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

  private final Random random = new Random();

  @Inject
  ApplicationProperties properties;

  @Inject
  RegistryEntityRepository registryRepository;

  @Inject
  OperationUtils utils;

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
  public RegistryEntity createRegistry(RegistryInfo info, TunnelServer server) {
    RegistryEntity entity = new RegistryEntity();
    {
      entity.setBoxRegKey("brk_" + unifiedRandomCharters(8));
      entity.setClientRegKey("crk_" + unifiedRandomCharters(8));
      entity.setBoxUUID(info.getBoxUUID());
      entity.setClientUUID(info.getClientUUID());
      entity.setSubdomain(info.getSubdomain() + "." + properties.getRegistrySubdomain());
      entity.setTunnelServer(utils.objectToJson(server));
    }
    registryRepository.persist(entity);
    return entity;
  }

  private String unifiedRandomCharters(int length) {
    int startChar = '0';
    int endChar = 'z';

    return random.ints(startChar, endChar + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

}
