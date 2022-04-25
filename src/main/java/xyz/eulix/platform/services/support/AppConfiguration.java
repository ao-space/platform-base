package xyz.eulix.platform.services.support;

import io.quarkus.runtime.Startup;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.network.service.NetworkService;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Startup
@ApplicationScoped
public class AppConfiguration {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Inject
    NetworkService networkService;

    @PostConstruct
    void init() {
        /**
         * 历史盒子添加用户面路由
         * 适用版本：1.0.2
         */
        LOG.infov("Application init begin...");
        LOG.infov("Cache NSR route begin...");
        // 查询全部用户域名
        List<SubdomainEntity> allSubdomains = subdomainEntityRepository.listAll();
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        // 添加用户面路由
        allSubdomains.forEach(subdomainEntity -> {
            try {
                networkService.cacheNSRoute(subdomainEntity.getUserDomain(), subdomainEntity.getBoxUUID());
                success.incrementAndGet();
            } catch (Exception e) {
                LOG.errorv(e, "Cache NSR route failed, userdomain:{0}", subdomainEntity.getUserDomain());
                failure.incrementAndGet();
            }
        });
        LOG.infov("Cache NSR route succeed! Total:{0}, success:{1}, fail:{2}", allSubdomains.size(), success.get() ,failure.get());

        /**
         * 创建默认目录
         */
        LOG.infov("Create directory begin...");
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
        LOG.infov("Create directory succeed!");
        LOG.infov("Application init succeed!");
    }
}
