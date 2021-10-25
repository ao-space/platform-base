package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.AppInfoEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppInfoEntityRepository implements PanacheRepository<AppInfoEntity> {
    // 根据主键查询资源
    private static final String FIND_BY_APPNAME_TYPE_VERSION = "app_name=?1 AND app_type=?2 AND app_version=?3";

    // 根据app_name、app_type查询资源，并根据app_version倒排
    private static final String FIND_BY_APPNAME_TYPE_VERSION_SORTED_BY_VERSION = "app_name=?1 AND app_type=?2 ORDER BY app_version DESC";

    // 根据app_name、app_type、app_version更新资源
    private static final String UPDATE_BY_APPNAME_TYPE_VERSION = "app_size=?1, update_desc=?2, force_update=?3, download_url=?4, md5=?5, extra=?6, updated_at=now() where app_name=?7 AND app_type=?8 AND app_version =?9";

    public void deleteByAppNameAndTypeAndVersion(String appName, String appType, String appVersion) {
        this.delete(FIND_BY_APPNAME_TYPE_VERSION, appName, appType, appVersion);
    }

    public AppInfoEntity findByAppNameAndTypeAndVersion(String appName, String appType, String curVersion) {
        return this.find(FIND_BY_APPNAME_TYPE_VERSION, appName, appType, curVersion).firstResult();
    }

    public AppInfoEntity findByAppNameAndTypeSortedByVersion(String appName, String appType) {
        return this.find(FIND_BY_APPNAME_TYPE_VERSION_SORTED_BY_VERSION, appName, appType).firstResult();
    }

    public void updateByAppNameAndTypeAndVersion(AppInfoEntity appInfoEntity) {
        this.update(UPDATE_BY_APPNAME_TYPE_VERSION, appInfoEntity.getAppSize(), appInfoEntity.getUpdateDesc(),
                appInfoEntity.getIsForceUpdate(), appInfoEntity.getDownloadUrl(), appInfoEntity.getMd5(), appInfoEntity.getExtra(),
                appInfoEntity.getAppName(), appInfoEntity.getAppType(), appInfoEntity.getAppVersion());
    }
}
