package xyz.eulix.platform.services;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
}
