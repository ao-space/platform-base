package xyz.eulix.platform.services.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Global quarkus configuration properties.
 *
 * <p>Impl ref:
 * <a _href=https://quarkus.io/guides/config-reference>https://quarkus.io/guides/config-reference</a>
 */
@ConfigProperties(prefix = "quarkus")
public interface QuarkusProperties {

    @ConfigProperty(name = "rest-client.upush-api.url")
    String getUPushHost();
}
