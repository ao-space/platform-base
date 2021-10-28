package xyz.eulix.platform.services.support;

import io.quarkus.runtime.Startup;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@Startup
@ApplicationScoped
public class AppConfiguration {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;

    @PostConstruct
    void init() {
        File dir = new File(properties.getFileLocation());
        if (!dir.exists()) {
            LOG.infov("Directory {0} does not exist, make it.", dir.getPath());
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                LOG.errorv("create directory:{0} failed", dir.getPath());
                throw new ServiceOperationException(ServiceError.DIR_CREATE_FAILED);
            }
        }
    }
}
