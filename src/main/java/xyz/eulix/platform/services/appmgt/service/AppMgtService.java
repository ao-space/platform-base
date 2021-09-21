package xyz.eulix.platform.services.appmgt.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.appmgt.dto.AppInfoCheckRes;
import xyz.eulix.platform.services.appmgt.dto.AppInfoReq;
import xyz.eulix.platform.services.appmgt.dto.AppInfoRes;
import xyz.eulix.platform.services.appmgt.entity.AppInfoEntity;
import xyz.eulix.platform.services.appmgt.repository.AppInfoEntityRepository;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class AppMgtService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    AppInfoEntityRepository appInfoEntityRepository;

    @Inject
    ApplicationProperties properties;

    /**
     * 新增app版本
     *
     * @param appInfoReq app版本信息
     * @return app版本信息
     */
    @Transactional
    public AppInfoRes saveAppinfo(AppInfoReq appInfoReq) {
        // 校验版本是否存在
        AppInfoEntity appInfoEntityOld = appInfoEntityRepository.findByAppNameAndTypeAndVersion(appInfoReq.getBundleId(),
                appInfoReq.getPlatform(), appInfoReq.getAppVersion());
        if (appInfoEntityOld != null) {
            LOG.debugv("illeagal app version, appName:{0}, appType:{1}, appVersion:{2}", appInfoReq.getBundleId(),
                    appInfoReq.getPlatform(), appInfoReq.getAppVersion());
            throw new ServiceOperationException(ServiceError.APP_VERSION_EXISTED);
        }
        AppInfoEntity appInfoEntity = appInfoReqToEntity(appInfoReq);
        appInfoEntityRepository.persist(appInfoEntity);

        return appInfoEntityToRes(appInfoEntity);
    }

    /**
     * 删除app版本
     *  @param appName app名称
     * @param appType app类型
     * @param appVersion app版本号
     */
    @Transactional
    public void delAppinfo(String appName, String appType, String appVersion) {
        appInfoEntityRepository.deleteByAppNameAndTypeAndVersion(appName, appType, appVersion);
    }

    /**
     * 检查app版本
     *
     * @param appName app名称
     * @param appType app类型
     * @param curVersion 当前版本
     * @return 是否更新
     */
    public AppInfoCheckRes checkAppInfo(String appName, String appType, String curVersion) {
        // 校验当前版本
        AppInfoEntity appInfoEntity = appInfoEntityRepository.findByAppNameAndTypeAndVersion(appName, appType, curVersion);
        if (appInfoEntity == null) {
            LOG.warnv("app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", appName, appType, curVersion);
            return AppInfoCheckRes.of(appName, appType, null, null, null, null, null);
        }
        // 查询最新版本
        AppInfoEntity newestAppInfoEntity = appInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        // 判断是否需要更新
        AppInfoCheckRes appInfoCheckRes = appInfoEntityToCheckRes(newestAppInfoEntity);
        if (appInfoEntity.getAppVersion().compareToIgnoreCase(newestAppInfoEntity.getAppVersion()) < 0) {
            LOG.infov("app version need to update, appName:{0}, appType:{1}, from curVersion:{2} to newVersion:{3}",
                    appName, appType, curVersion, newestAppInfoEntity.getAppVersion());
            appInfoCheckRes.setIsUpdate(true);
            // 判断当前版本是否需要特殊处理 || currentVersion < minVersion
            String minVersion = properties.getForceUpdateMinVersion();
            if (appInfoEntity.getIsForceUpdate() || appInfoEntity.getAppVersion().compareToIgnoreCase(minVersion) < 0) {
                LOG.infov("app version need to update mandatory, appName:{0}, appType:{1}, appVersion:{2}", appName,
                        appType, curVersion);
                appInfoCheckRes.setIsForceUpdate(true);
            }
            return appInfoCheckRes;
        }
        return appInfoCheckRes;
    }

    private AppInfoCheckRes appInfoEntityToCheckRes(AppInfoEntity appInfoEntity) {
        return AppInfoCheckRes.of(appInfoEntity.getAppName(),
                appInfoEntity.getAppType(),
                appInfoEntity.getAppVersion(),
                appInfoEntity.getAppSize(),
                appInfoEntity.getDownloadUrl(),
                appInfoEntity.getUpdateDesc(),
                appInfoEntity.getMd5());
    }

    private AppInfoRes appInfoEntityToRes(AppInfoEntity appInfoEntity) {
        return AppInfoRes.of(appInfoEntity.getAppName(),
                appInfoEntity.getAppType(),
                appInfoEntity.getAppVersion(),
                appInfoEntity.getAppSize(),
                appInfoEntity.getDownloadUrl(),
                appInfoEntity.getUpdateDesc(),
                appInfoEntity.getMd5(),
                appInfoEntity.getIsForceUpdate());
    }

    private AppInfoEntity appInfoReqToEntity(AppInfoReq appInfoReq) {
        return AppInfoEntity.of(appInfoReq.getBundleId(),
                appInfoReq.getPlatform(),
                appInfoReq.getAppVersion(),
                appInfoReq.getAppSize(),
                appInfoReq.getDownloadUrl(),
                appInfoReq.getUpdateDesc(),
                appInfoReq.getMd5(),
                appInfoReq.getIsForceUpdate(), null);
    }
}
