package xyz.eulix.platform.services.provider.impl;

import io.quarkus.runtime.Startup;
import xyz.eulix.platform.services.network.entity.NetworkServerEntity;
import xyz.eulix.platform.services.network.repository.NetworkServerEntityRepository;
import xyz.eulix.platform.services.provider.ProviderFactory;
import xyz.eulix.platform.services.provider.inf.RegistryProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Startup
@ApplicationScoped
public class DefaultRegistryProvider implements RegistryProvider {
    private static final String PROVIDER_NAME = "default";

    @Inject
    NetworkServerEntityRepository serverEntityRepository;

    @PostConstruct
    void init() {
        ProviderFactory.putRegisterProvider(PROVIDER_NAME, this);
    }

    @Override
    public Boolean isBoxIllegal(String boxUUID) {
        return true;
    }

    @Override
    public Long calculateNetworkRoute(String networkClientId) {
        // 查询server列表
        List<NetworkServerEntity> serverEntities = serverEntityRepository.findAll().list();
        // hash算法
        NetworkServerEntity serverEntity = allocateAlgotithm(serverEntities, networkClientId);
        return serverEntity.getId();
    }

    private NetworkServerEntity allocateAlgotithm(List<NetworkServerEntity> serverEntities, String networkClientId) {
        int hash = networkClientId.hashCode();
        int index = (hash & Integer.MAX_VALUE) % serverEntities.size();
        return serverEntities.get(index);
    }

    @Override
    public Boolean isSubdomainIllegal(String subdomain) {
        return true;
    }
}
