package xyz.eulix.platform.services.registry.service;

import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class RegistryService {

  @Inject
  RegistryEntityRepository registryRepository;

  public boolean verifyClient(String clientRegKey, String clientUUID) {
    Optional<RegistryEntity> rp = registryRepository.find(
        "client_uuid", clientUUID).singleResultOptional();
    return rp.filter(r -> clientRegKey.equals(r.getClientRegKey())).isPresent();
  }

}
