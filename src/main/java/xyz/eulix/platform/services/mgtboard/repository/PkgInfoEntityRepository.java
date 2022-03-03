package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

import io.quarkus.panache.common.Sort;
import xyz.eulix.platform.services.mgtboard.dto.SortKeyEnum;
import xyz.eulix.platform.services.mgtboard.entity.PkgInfoEntity;
import xyz.eulix.platform.services.support.model.SortDirEnum;

import java.util.List;

@ApplicationScoped
public class PkgInfoEntityRepository implements PanacheRepository<PkgInfoEntity> {
    // 根据主键查询资源
    private static final String FIND_BY_APPNAME_TYPE_VERSION = "pkg_name=?1 AND pkg_type=?2 AND pkg_version=?3";

    // 根据pkg_name、pkg_type查询资源
    private static final String FIND_BY_APPNAME_TYPE = "pkg_name=?1 AND pkg_type=?2";

    // 根据pkg_name、pkg_type、pkg_version更新资源
    private static final String UPDATE_BY_APPNAME_TYPE_VERSION = "pkg_size=?1, update_desc=?2, force_update=?3, download_url=?4, md5=?5, "
        + "min_compatible_android_version=?6 ,min_compatible_ios_version=?7 ,min_compatible_box_version=?8 ,extra=?9, updated_at=now() "
        + "where pkg_name=?10 AND pkg_type=?11 AND pkg_version =?12";

    // 根据ids查询资源
    private static final String FIND_BY_IDS = "id in (?1)";

    public void deleteByAppNameAndTypeAndVersion(String pkgName, String pkgType, String pkgVersion) {
        this.delete(FIND_BY_APPNAME_TYPE_VERSION, pkgName, pkgType, pkgVersion);
    }

    public PkgInfoEntity findByAppNameAndTypeAndVersion(String pkgName, String pkgType, String curVersion) {
        return this.find(FIND_BY_APPNAME_TYPE_VERSION, pkgName, pkgType, curVersion).firstResult();
    }

    public List<PkgInfoEntity> findByAppNameAndType(String pkgName, String pkgType) {
        return this.find(FIND_BY_APPNAME_TYPE, pkgName, pkgType).list();
    }

    public void updateByAppNameAndTypeAndVersion(PkgInfoEntity pkgInfoEntity) {
        this.update(UPDATE_BY_APPNAME_TYPE_VERSION, pkgInfoEntity.getPkgSize(), pkgInfoEntity.getUpdateDesc(),
            pkgInfoEntity.getIsForceUpdate(), pkgInfoEntity.getDownloadUrl(), pkgInfoEntity.getMd5(), pkgInfoEntity.getMinCompatibleAndroidVersion(),
            pkgInfoEntity.getMinCompatibleIOSVersion(), pkgInfoEntity.getMinCompatibleBoxVersion(), pkgInfoEntity.getExtra(),
            pkgInfoEntity.getPkgName(), pkgInfoEntity.getPkgType(), pkgInfoEntity.getPkgVersion());
    }

    public void deleteByPkgIds(List<Long> packageIds) {
        this.delete(FIND_BY_IDS, packageIds);
    }

    public List<PkgInfoEntity> sortByPkgType(String sortKey, String sortDir, Integer currentPage, Integer pageSize) {
        if (SortDirEnum.ASC.getName().equals(sortDir)) {
            return this.findAll(Sort.by(sortKey, Sort.Direction.Ascending)).page(currentPage, pageSize).list();
        } else {
            return this.findAll(Sort.by(sortKey, Sort.Direction.Descending)).page(currentPage, pageSize).list();
        }
    }
}
