package xyz.eulix.platform.services.auth.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.auth.entity.PkeyAuthEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PkeyAuthEntityRepository implements PanacheRepository<PkeyAuthEntity> {

    public PkeyAuthEntity findByPkey(String pkey) {
        return this.find("pkey", pkey).firstResult();
    }
}
