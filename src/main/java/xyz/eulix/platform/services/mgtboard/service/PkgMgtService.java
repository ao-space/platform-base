package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.PackageCheckRes;
import xyz.eulix.platform.services.mgtboard.dto.PackageReq;
import xyz.eulix.platform.services.mgtboard.dto.PackageRes;
import xyz.eulix.platform.services.mgtboard.entity.PkgInfoEntity;
import xyz.eulix.platform.services.mgtboard.repository.PkgInfoEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class PkgMgtService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    PkgInfoEntityRepository pkgInfoEntityRepository;

    /**
     * 新增app版本
     *
     * @param packageReq app版本信息
     * @return app版本信息
     */
    @Transactional
    public PackageRes savePkgInfo(PackageReq packageReq) {
        // 校验版本是否存在
        PkgInfoEntity PkgInfoEntityOld = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(packageReq.getPkgName(),
                packageReq.getPkgType(), packageReq.getPkgVersion());
        if (PkgInfoEntityOld != null) {
            LOG.warnv("app version already exist, appName:{0}, appType:{1}, appVersion:{2}", packageReq.getPkgName(),
                    packageReq.getPkgType(), packageReq.getPkgVersion());
            throw new ServiceOperationException(ServiceError.APP_VERSION_EXISTED);
        }
        PkgInfoEntity PkgInfoEntity = pkgInfoReqToEntity(packageReq);
        pkgInfoEntityRepository.persist(PkgInfoEntity);

        return pkgInfoEntityToRes(PkgInfoEntity);
    }


    /**
     * 更新app版本
     *
     * @param packageReq app版本信息
     * @return app版本信息
     */
    @Transactional
    public PackageRes updateAppinfo(PackageReq packageReq) {
        // 校验版本是否存在
        PkgInfoEntity PkgInfoEntityOld = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(packageReq.getPkgName(),
                packageReq.getPkgType(), packageReq.getPkgVersion());
        if (PkgInfoEntityOld == null) {
            LOG.warnv("app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", packageReq.getPkgName(),
                    packageReq.getPkgType(), packageReq.getPkgVersion());
            throw new ServiceOperationException(ServiceError.APP_VERSION_NOT_EXIST);
        }
        PkgInfoEntity PkgInfoEntity = pkgInfoReqToEntity(packageReq);
        pkgInfoEntityRepository.updateByAppNameAndTypeAndVersion(PkgInfoEntity);
        return pkgInfoEntityToRes(PkgInfoEntity);
    }

    /**
     * 删除app版本
     *  @param appName app名称
     * @param appType app类型
     * @param appVersion app版本号
     */
    @Transactional
    public void delAppinfo(String appName, String appType, String appVersion) {
        pkgInfoEntityRepository.deleteByAppNameAndTypeAndVersion(appName, appType, appVersion);
    }


    /**
     * 检查 App 版本
     * @param appName app name
     * @param appType app 类型
     * @param curBoxVersion 盒子版本
     * @param curAppVersion App版本
     * @return 检查结果
     */
    public PackageCheckRes checkAppInfo(String appName, String appType, String curBoxVersion, String curAppVersion) {
        // 查询当前 app 版本
        PkgInfoEntity curAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(appName, appType, curAppVersion);
        if (curAppPkg == null){
            LOG.warnv("app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", appName, appType, curAppVersion);
            return PackageCheckRes.of(null, null);
        }
        // 查询最新 app、box 版本
        PkgInfoEntity latestBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);

        // 查询最新 app 版本
        PkgInfoEntity latestAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        // 判断是否需要更新
        if (curAppVersion.compareToIgnoreCase(latestAppPkg.getPkgVersion()) < 0) {
            LOG.infov(
                "app version need to update, appName:{0}, appType:{1}, from curVersion:{2} to newVersion:{3}",
                appName, appType, curAppVersion, latestAppPkg.getPkgVersion());
            return PackageCheckRes.of(pkgInfoEntityToRes(latestAppPkg), null);
        }

        // 判断 app 新版本与当前 box 版本兼容性
        if (curBoxVersion.compareToIgnoreCase(latestAppPkg.getMinCompatibleBoxVersion())  < 0) {
            // 当前盒子的版本比最新 app 兼容 box 版本低
            PackageCheckRes packageCheckRes = PackageCheckRes.of(pkgInfoEntityToRes(latestAppPkg), pkgInfoEntityToRes(latestBoxPkg));
            packageCheckRes.setNewVersionExist(true);
            packageCheckRes.setIsBoxNeedUpdate(true);
            return packageCheckRes;
        }
        PackageCheckRes packageCheckRes = PackageCheckRes.of(pkgInfoEntityToRes(latestAppPkg), pkgInfoEntityToRes(latestBoxPkg));
        packageCheckRes.setNewVersionExist(true);

        return packageCheckRes;
    }
    /**
     * 检查 Box 版本
     * @param boxName Box name
     * @param boxType Box 类型
     * @param curBoxVersion 盒子版本
     * @param curAppVersion App版本
     * @return 检查结果
     */
    public PackageCheckRes checkBoxInfo(String boxName, String boxType, String curBoxVersion, String curAppVersion, String appType) {
        // 查询当前 Box 版本
        PkgInfoEntity curBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(boxName, boxType, curBoxVersion);
        if (curBoxPkg == null){
            LOG.warnv("box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", boxName, boxType, curBoxVersion);
            return PackageCheckRes.of(null, null);
        }
        // 查询最新 box 版本
        PkgInfoEntity latestBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(boxName, boxType);
        // 判断是否需要更新
        if (curAppVersion.compareToIgnoreCase(latestBoxPkg.getPkgVersion()) < 0) {
            LOG.infov(
                "box version need to update, boxName:{0}, boxType:{1}, from curVersion:{2} to newVersion:{3}",
                boxName, boxType, curBoxVersion, latestBoxPkg.getPkgVersion());
            return PackageCheckRes.of(null, pkgInfoEntityToRes(latestBoxPkg));
        }
        // 查询最新 box 版本
        PkgInfoEntity latestAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(boxName, boxType);

        String latestMinAppVersion;
        if("ios".equals(appType)){
            latestMinAppVersion = latestBoxPkg.getMinCompatibleIOSVersion();
        }else {
            latestMinAppVersion = latestBoxPkg.getMinCompatibleAndroidVersion();
        }
        // 判断 box 新版本与当前 app 版本兼容性
        if (curAppVersion.compareToIgnoreCase(latestMinAppVersion)  < 0) {
            // 当前 app 的版本比最新 box 兼容 app 版本低
            PackageCheckRes packageCheckRes = PackageCheckRes.of(pkgInfoEntityToRes(latestAppPkg), pkgInfoEntityToRes(latestBoxPkg));
            packageCheckRes.setNewVersionExist(true);
            packageCheckRes.setIsAppNeedUpdate(true);
            return packageCheckRes;
        }
        PackageCheckRes packageCheckRes = PackageCheckRes.of(pkgInfoEntityToRes(latestAppPkg), pkgInfoEntityToRes(latestBoxPkg));
        packageCheckRes.setNewVersionExist(true);

        return packageCheckRes;
    }

    private PackageRes pkgInfoEntityToRes(PkgInfoEntity PkgInfoEntity) {
        return PackageRes.of(PkgInfoEntity.getPkgName(),
                PkgInfoEntity.getPkgType(),
                PkgInfoEntity.getPkgVersion(),
                PkgInfoEntity.getPkgSize(),
                PkgInfoEntity.getDownloadUrl(),
                PkgInfoEntity.getUpdateDesc(),
                PkgInfoEntity.getMd5(),
                PkgInfoEntity.getIsForceUpdate(),
                PkgInfoEntity.getMinCompatibleAndroidVersion(),
                PkgInfoEntity.getMinCompatibleIOSVersion(),
                PkgInfoEntity.getMinCompatibleBoxVersion());
    }

    private PkgInfoEntity pkgInfoReqToEntity(PackageReq packageReq) {
        return PkgInfoEntity.of(packageReq.getPkgName(),
                packageReq.getPkgType(),
                packageReq.getPkgVersion(),
                packageReq.getPkgSize(),
                packageReq.getUpdateDesc(),
                packageReq.getIsForceUpdate(),
                packageReq.getDownloadUrl(),
                packageReq.getMd5(),
                packageReq.getMinAndroidVersion(),
                packageReq.getMinIOSVersion(),
                packageReq.getMinBoxVersion(), null);
    }


}
