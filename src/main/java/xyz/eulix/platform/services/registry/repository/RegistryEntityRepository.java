package xyz.eulix.platform.services.registry.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import xyz.eulix.platform.services.registry.entity.RegistryEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NonUniqueResultException;
import java.util.Optional;

import static xyz.eulix.platform.services.registry.entity.RegistryEntity.State.*;

/**
 * All Registry Entity related storage operations including standard CRUD and
 * other customized operations such as state transition and updating.
 */
@ApplicationScoped
public class RegistryEntityRepository implements PanacheRepository<RegistryEntity> {
}
