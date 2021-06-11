package xyz.eulix.platform.services.registry.service;

import xyz.eulix.platform.services.registry.entity.RegistryEntity;
import xyz.eulix.platform.services.registry.repository.RegistryEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides activation key related registry service.
 */
@ApplicationScoped
public class ActivationRegistryService {

  @Inject
  RegistryEntityRepository registryRepository;

  public boolean verifyClient(String clientRegKey, String clientUUID) {
    Optional<RegistryEntity> rp = registryRepository.find(
        "client_uuid", clientUUID).singleResultOptional();
    return rp.filter(r -> clientRegKey.endsWith(r.getClientRegKey())).isPresent();
  }

}
