package xyz.eulix.platform.services.provider;

import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.provider.inf.RegistryProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ProviderFactory {
    private static final String PROVIDER_NAME = "default";
    private static final Map<String, RegistryProvider> map = new ConcurrentHashMap<>();

    @Inject
    ApplicationProperties properties;

    public static void putRegisterProvider(String name, RegistryProvider registryProvider){
        map.put(name, registryProvider);
    }

    public RegistryProvider getRegistryProvider() {
        String name = properties.getRegisterProviderName();
        if (map.containsKey(name)) {
            return map.get(name);
        } else {
            return map.get(PROVIDER_NAME);
        }
    }
}
