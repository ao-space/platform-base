package xyz.eulix.platform.services.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Global application configuration properties.
 *
 * <p>Impl ref:
 * <a _href=https://quarkus.io/guides/config-reference>https://quarkus.io/guides/config-reference</a>
 */
@ConfigProperties(prefix = "app")
public interface ApplicationProperties {

    @ConfigProperty
    String getVersion();

    @ConfigProperty(name = "registry.subdomain")
    String getRegistrySubdomain();

    @ConfigProperty(name = "registry.tunnel-server.base-url")
    String getRegistryTunnelServerBaseUrl();

    @ConfigProperty(name = "registry.tunnel-server.port")
    int getRegistryTunnelServerPort();

    @ConfigProperty(name = "constant.speed-of-sound-in-meter-per-second", defaultValue = "343")
    int getSpeedOfSound();

    @ConfigProperty(name = "display.mach")
    Optional<Integer> getDisplayMach();

    @ConfigProperty(name = "display.unit.name")
    String getDisplayUnitName();

    @ConfigProperty(name = "display.unit.factor")
    BigDecimal getDisplayUnitFactor();

    @ConfigProperty(name = "forceupdate.minVersion")
    String getForceUpdateMinVersion();
}
