package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.AppInfoCheckRes;
import xyz.eulix.platform.services.mgtboard.dto.PackageCheckRes;
import xyz.eulix.platform.services.mgtboard.dto.PackageReq;
import xyz.eulix.platform.services.mgtboard.dto.PackageRes;
import xyz.eulix.platform.services.mgtboard.dto.PkgActionEnum;
import xyz.eulix.platform.services.mgtboard.entity.PkgInfoEntity;
import xyz.eulix.platform.services.mgtboard.repository.PkgInfoEntityRepository;
import xyz.eulix.platform.services.config.ApplicationProperties;
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

    @Inject
    ApplicationProperties properties;

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

    public PackageCheckRes checkPkgInfo(String action, String pkgName, String pkgType, String curBoxVersion, String curAppVersion) {

        switch (action){
            case "app_check": return checkAppInfo(pkgName, pkgType, curBoxVersion, curAppVersion);
            case "box_check": return checkBoxInfo(pkgName, pkgType, curBoxVersion, curAppVersion);
        }
        return PackageCheckRes.of(null, null);
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
        PkgInfoEntity curAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(appName, appType, curAppVersion);;
        // 查询最新 app、box 版本
        PkgInfoEntity latestAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        PkgInfoEntity latestBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        PackageCheckRes packageCheckRes = PackageCheckRes.of(latestAppPkg,latestBoxPkg);

        // 校验当前 box 版本
        if (curAppPkg == null) {
            LOG.warnv("box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", appName, appType, curAppVersion);
            return PackageCheckRes.of(null,null);
        }

        // 判断是否需要更新
        if (curAppVersion.compareToIgnoreCase(latestAppPkg.getPkgVersion()) < 0) {
            LOG.infov(
                "app version need to update, appName:{0}, appType:{1}, from curVersion:{2} to newVersion:{3}",
                appName, appType, curAppVersion, latestAppPkg.getPkgVersion());
            packageCheckRes.setNewVersionExist(true);
            // 判断 box 新版本与当前 app 版本 兼容性 ；判断当前版本是否需要特殊处理 || currentVersion < minVersion
            if(latestBoxPkg.getIsForceUpdate()){
                packageCheckRes.setIsBoxNeedUpdate(true);
            }
        }

        return packageCheckRes;
    }
    /**
     * 检查 Box 版本
     * @param appName Box name
     * @param appType Box 类型
     * @param curBoxVersion 盒子版本
     * @param curAppVersion App版本
     * @return 检查结果
     */
    public PackageCheckRes checkBoxInfo(String appName, String appType, String curBoxVersion, String curAppVersion) {
        // 查询当前 box 版本
        PkgInfoEntity curBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(appName, appType, curBoxVersion);;
        // 查询最新 app、box 版本
        PkgInfoEntity latestAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        PkgInfoEntity latestBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeSortedByVersion(appName, appType);
        PackageCheckRes packageCheckRes = PackageCheckRes.of(latestAppPkg,latestBoxPkg);

        // 校验当前 box 版本
        if (curBoxPkg == null) {
            LOG.warnv("box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", appName, appType, curBoxVersion);
            return PackageCheckRes.of(null,null);
        }

        // 判断是否需要更新
        if (curBoxVersion.compareToIgnoreCase(latestBoxPkg.getPkgVersion()) < 0) {
            LOG.infov(
                "app version need to update, appName:{0}, appType:{1}, from curVersion:{2} to newVersion:{3}",
                appName, appType, curBoxVersion, latestBoxPkg.getPkgVersion());
            packageCheckRes.setNewVersionExist(true);
            // 判断 box 新版本与当前 app 版本 兼容性 ；判断当前版本是否需要特殊处理 || currentVersion < minVersion
            if(latestBoxPkg.getIsForceUpdate()){
                packageCheckRes.setIsBoxNeedUpdate(true);
            }
        }

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
            PkgInfoEntity.getMinCompatibleVersion());
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
                packageReq.getMinAppVersion(),null);
    }


}
