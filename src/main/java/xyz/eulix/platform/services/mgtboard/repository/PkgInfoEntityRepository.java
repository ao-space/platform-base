package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;
import xyz.eulix.platform.services.mgtboard.entity.PkgInfoEntity;

@ApplicationScoped
public class PkgInfoEntityRepository implements PanacheRepository<PkgInfoEntity> {
    // 根据主键查询资源
    private static final String FIND_BY_APPNAME_TYPE_VERSION = "pkg_name=?1 AND pkg_type=?2 AND pkg_version=?3";

    // 根据pkg_name、pkg_type查询资源，并根据pkg_version倒排
    private static final String FIND_BY_APPNAME_TYPE_VERSION_SORTED_BY_VERSION = "pkg_name=?1 AND pkg_type=?2 ORDER BY pkg_version DESC";

    // 根据pkg_name、pkg_type、pkg_version更新资源
    private static final String UPDATE_BY_APPNAME_TYPE_VERSION = "pkg_size=?1, update_desc=?2, force_update=?3, download_url=?4, md5=?5, min_compatible_version=?6 ,extra=?7, updated_at=now() where pkg_name=?8 AND pkg_type=?9 AND pkg_version =?10";

    public void deleteByAppNameAndTypeAndVersion(String pkgName, String pkgType, String pkgVersion) {
        this.delete(FIND_BY_APPNAME_TYPE_VERSION, pkgName, pkgType, pkgVersion);
    }

    public PkgInfoEntity findByAppNameAndTypeAndVersion(String pkgName, String pkgType, String curVersion) {
        return this.find(FIND_BY_APPNAME_TYPE_VERSION, pkgName, pkgType, curVersion).firstResult();
    }

    public PkgInfoEntity findByAppNameAndTypeSortedByVersion(String pkgName, String pkgType) {
        return this.find(FIND_BY_APPNAME_TYPE_VERSION_SORTED_BY_VERSION, pkgName, pkgType).firstResult();
    }

    public void updateByAppNameAndTypeAndVersion(PkgInfoEntity pkgInfoEntity) {
        this.update(UPDATE_BY_APPNAME_TYPE_VERSION, pkgInfoEntity.getPkgSize(), pkgInfoEntity.getUpdateDesc(),
            pkgInfoEntity.getIsForceUpdate(), pkgInfoEntity.getDownloadUrl(), pkgInfoEntity.getMd5(), pkgInfoEntity.getMinCompatibleVersion() ,pkgInfoEntity.getExtra(),
            pkgInfoEntity.getPkgName(), pkgInfoEntity.getPkgType(), pkgInfoEntity.getPkgVersion());
    }
}
