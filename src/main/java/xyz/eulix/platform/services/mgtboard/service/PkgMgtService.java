package xyz.eulix.platform.services.mgtboard.service;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.entity.PkgInfoEntity;
import xyz.eulix.platform.services.mgtboard.entity.ProposalEntity;
import xyz.eulix.platform.services.mgtboard.repository.PkgInfoEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PkgMgtService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    PkgInfoEntityRepository pkgInfoEntityRepository;

    @Inject
    ApplicationProperties applicationProperties;

    /**
     * 新增pkg版本
     *
     * @param packageReq pkg版本信息
     * @return pkg版本信息
     */
    @Transactional
    public PackageRes savePkgInfo(PackageReq packageReq) {
        // 版本号校验
        isValidVersionThrowEx(packageReq);
        // 校验版本是否存在
        PkgInfoEntity PkgInfoEntityOld = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(packageReq.getPkgName(),
                packageReq.getPkgType(), packageReq.getPkgVersion());
        if (PkgInfoEntityOld != null) {
            LOG.warnv("pkg version already exist, pkgName:{0}, pkgType:{1}, pkgVersion:{2}", packageReq.getPkgName(),
                    packageReq.getPkgType(), packageReq.getPkgVersion());
            throw new ServiceOperationException(ServiceError.PKG_VERSION_EXISTED);
        }
        PkgInfoEntity PkgInfoEntity = pkgInfoReqToEntity(packageReq);
        pkgInfoEntityRepository.persist(PkgInfoEntity);

        return pkgInfoEntityToRes(PkgInfoEntity);
    }

    public void isValidVersionThrowEx(PackageReq packageReq) {
        isValidVersionThrowEx(packageReq.getPkgVersion(), "packageReq.pkgVersion");
        if (!CommonUtils.isNullOrEmpty(packageReq.getMinBoxVersion())) {
            isValidVersionThrowEx(packageReq.getMinBoxVersion(), "packageReq.minBoxVersion");
        }
        if (!CommonUtils.isNullOrEmpty(packageReq.getMinAndroidVersion())) {
            isValidVersionThrowEx(packageReq.getMinAndroidVersion(), "packageReq.minAndroidVersion");
        }
        if (!CommonUtils.isNullOrEmpty(packageReq.getMinIOSVersion())) {
            isValidVersionThrowEx(packageReq.getMinIOSVersion(), "packageReq.minIOSVersion");
        }
    }

    public void isValidVersionThrowEx(String version, String versionName) {
        try {
            Version.valueOf(version);
        } catch (IllegalArgumentException | ParseException e) {
            LOG.warnv("version {0} is illegal, value:{1}", versionName, version);
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, versionName);
        }
    }

    /**
     * 更新pkg版本
     *
     * @param packageReq pkg版本信息
     * @return pkg版本信息
     */
    @Transactional
    public PackageRes updatePkginfo(PackageReq packageReq) {
        // 版本号校验
        isValidVersionThrowEx(packageReq);
        // 校验版本是否存在
        PkgInfoEntity PkgInfoEntityOld = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(packageReq.getPkgName(),
                packageReq.getPkgType(), packageReq.getPkgVersion());
        if (PkgInfoEntityOld == null) {
            LOG.warnv("pkg version does not exist, pkgName:{0}, pkgType:{1}, pkgVersion:{2}", packageReq.getPkgName(),
                    packageReq.getPkgType(), packageReq.getPkgVersion());
            throw new ServiceOperationException(ServiceError.PKG_VERSION_NOT_EXIST);
        }
        PkgInfoEntity PkgInfoEntity = pkgInfoReqToEntity(packageReq);
        pkgInfoEntityRepository.updateByAppNameAndTypeAndVersion(PkgInfoEntity);
        return pkgInfoEntityToRes(PkgInfoEntity);
    }

    /**
     * 删除pkg版本
     *
     * @param pkgName    pkg名称
     * @param pkgType    pkg类型
     * @param pkgVersion pkg版本号
     */
    @Transactional
    public void delPkginfo(String pkgName, String pkgType, String pkgVersion) {
        pkgInfoEntityRepository.deleteByAppNameAndTypeAndVersion(pkgName, pkgType, pkgVersion);
    }

    /**
     * 查询pkg版本
     *
     * @param pkgName    pkg名称
     * @param pkgType    pkg类型
     * @param pkgVersion pkg版本号
     * @return pkg版本
     */
    public PackageRes getPkgInfo(String pkgName, String pkgType, String pkgVersion) {
        PkgInfoEntity pkgInfoEntity = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(pkgName, pkgType, pkgVersion);
        if (pkgInfoEntity == null) {
            LOG.warnv("pkg version does not exist, pkgName:{0}, pkgType:{1}, pkgVersion:{2}", pkgName, pkgType, pkgVersion);
            throw new ServiceOperationException(ServiceError.PKG_VERSION_NOT_EXIST);
        }
        return pkgInfoEntityToRes(pkgInfoEntity);
    }


    /**
     * 检查 App 版本
     *
     * @param appName       app name
     * @param appType       app 类型
     * @param curBoxVersion 盒子版本
     * @param curAppVersion App版本
     * @return 检查结果
     */
    public PackageCheckRes checkAppInfo(String appName, String appType, String curAppVersion, String boxName, String boxType, String curBoxVersion) {
        PackageCheckRes packageCheckRes = new PackageCheckRes();

        // 查询当前 app 版本
        PkgInfoEntity curAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(appName, appType, curAppVersion);
        if (curAppPkg == null) {
            LOG.warnv("app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", appName, appType, curAppVersion);
            throw new ServiceOperationException(ServiceError.PKG_VERSION_NOT_EXIST);
        }
        // 查询最新 app、box 版本
        PkgInfoEntity latestBoxPkg = findByAppNameAndTypeSortedByVersion(boxName, boxType);
        PkgInfoEntity latestAppPkg = findByAppNameAndTypeSortedByVersion(appName, appType);

        // 判断是否需要更新
        if (isLowerVersion(curAppVersion, latestAppPkg.getPkgVersion())) {
            LOG.infov(
                    "app version need to update, appName:{0}, appType:{1}, from curVersion:{2} to newVersion:{3}",
                    appName, appType, curAppVersion, latestAppPkg.getPkgVersion());
            packageCheckRes.setLatestAppPkg(pkgInfoEntityToRes(latestAppPkg));
            packageCheckRes.setNewVersionExist(true);

            // 判断 app 新版本与当前 box 版本兼容性
            if (isLowerVersion(curBoxVersion, latestAppPkg.getMinCompatibleBoxVersion())) {
                // 当前盒子的版本比最新 app 兼容 box 版本低
                packageCheckRes.setLatestBoxPkg(pkgInfoEntityToRes(latestBoxPkg));
                packageCheckRes.setIsBoxNeedUpdate(true);
            }
        }
        return packageCheckRes;
    }

    /**
     * 检查 Box 版本
     *
     * @param boxName       Box name
     * @param curBoxVersion 盒子版本
     * @param curAppVersion App版本
     * @return 检查结果
     */
    public PackageCheckRes checkBoxInfo(String appName, String appType, String curAppVersion, String boxName, String boxType, String curBoxVersion) {
        PackageCheckRes packageCheckRes = new PackageCheckRes();

        // 查询当前 Box 版本
        PkgInfoEntity curBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(boxName, boxType, curBoxVersion);
        if (curBoxPkg == null) {
            LOG.warnv("box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", boxName, boxType, curBoxVersion);
            throw new ServiceOperationException(ServiceError.PKG_VERSION_NOT_EXIST);
        }
        // 查询最新 box 版本
        PkgInfoEntity latestBoxPkg = findByAppNameAndTypeSortedByVersion(boxName, boxType);
        // 查询最新 app 版本
        PkgInfoEntity latestAppPkg = findByAppNameAndTypeSortedByVersion(appName, appType);
        String latestMinAppVersion;
        if ("ios".equals(appType)) {
            latestMinAppVersion = latestBoxPkg.getMinCompatibleIOSVersion();
        } else {
            latestMinAppVersion = latestBoxPkg.getMinCompatibleAndroidVersion();
        }

        // 判断是否需要更新
        if (isLowerVersion(curBoxVersion, latestBoxPkg.getPkgVersion())) {
            LOG.infov(
                    "box version need to update, boxName:{0}, boxType:{1}, from curVersion:{2} to newVersion:{3}",
                    boxName, "box", curBoxVersion, latestBoxPkg.getPkgVersion());
            packageCheckRes.setLatestBoxPkg(pkgInfoEntityToRes(latestBoxPkg));
            packageCheckRes.setNewVersionExist(true);
            // 判断 box 新版本与当前 app 版本兼容性
            if (isLowerVersion(curAppVersion, latestMinAppVersion)) {
                // 当前 app 的版本比最新 box 兼容 app 版本低
                packageCheckRes.setLatestAppPkg(pkgInfoEntityToRes(latestAppPkg));
                packageCheckRes.setIsAppNeedUpdate(true);
            }
        }

        return packageCheckRes;
    }

    /**
     * 强制升级 &兼容性 检测
     *
     * @param appPkgName    app名称
     * @param appPkgType    app类型
     * @param curAppVersion app当前版本
     * @param boxPkgName    box名称
     * @param boxPkgType    box类型
     * @param curBoxVersion box当前版本
     * @return 是否需要强制更新
     */
    public CompatibleCheckRes compatibleCheck(String appPkgName, String appPkgType, String curAppVersion, String boxPkgName,
                                              String boxPkgType, String curBoxVersion) {
        CompatibleCheckRes compatibleCheckRes = CompatibleCheckRes.of();
        // 0.校验当前版本是否存在
        PkgInfoEntity curAppPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(appPkgName, appPkgType, curAppVersion);
        if (curAppPkg == null) {
            LOG.warnv("app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", appPkgName, appPkgType, curAppVersion);
            return compatibleCheckRes;
        }
        PkgInfoEntity curBoxPkg = pkgInfoEntityRepository.findByAppNameAndTypeAndVersion(boxPkgName, boxPkgType, curBoxVersion);
        if (curBoxPkg == null) {
            LOG.warnv("box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", boxPkgName, boxPkgType, curBoxVersion);
            return compatibleCheckRes;
        }

        PkgInfoEntity targetAppPkgInfo = curAppPkg;     // 目标app版本=当前app版本
        PkgInfoEntity targetBoxPkgInfo = curBoxPkg;     // 目标box版本=当前box版本
        // 查询最新 app 版本
        PkgInfoEntity latestAppPkg = findByAppNameAndTypeSortedByVersion(appPkgName, appPkgType);
        // 查询最新 box 版本
        PkgInfoEntity latestBoxPkg = findByAppNameAndTypeSortedByVersion(boxPkgName, boxPkgType);
        // 1.检查当前app版本是否需要强制升级
        if (curAppPkg.getIsForceUpdate()) {
            if (!isLowerVersion(curAppVersion, latestAppPkg.getPkgVersion())) {
                LOG.errorv("latest app version does not exist, appName:{0}, appType:{1}, appVersion:{2}", appPkgName, appPkgType, curAppVersion);
                throw new ServiceOperationException(ServiceError.LATEST_APP_VERSION_NOT_EXIST);
            }
            targetAppPkgInfo = latestAppPkg;    // 目标app版本=最新app版本
            compatibleCheckRes.setIsAppForceUpdate(true);
            compatibleCheckRes.setLastestAppPkg(pkgInfoEntityToRes(latestAppPkg));
            LOG.infov("app version needs to force upgrade, appName:{0}, appType:{1}, appVersion:{2}", appPkgName, appPkgType, curAppVersion);
        }
        // 2.检查当前box版本是否需要强制升级
        if (curBoxPkg.getIsForceUpdate()) {
            if (!isLowerVersion(curBoxVersion, latestBoxPkg.getPkgVersion())) {
                LOG.errorv("latest box version does not exist, boxName:{0}, boxType:{1}, boxVersion:{2}", boxPkgName, boxPkgType, curBoxVersion);
                throw new ServiceOperationException(ServiceError.LATEST_BOX_VERSION_NOT_EXIST);
            }
            targetBoxPkgInfo = latestBoxPkg;    // 目标box版本=最新box版本
            compatibleCheckRes.setIsBoxForceUpdate(true);
            compatibleCheckRes.setLastestBoxPkg(pkgInfoEntityToRes(latestBoxPkg));
            LOG.infov("box version needs to force upgrade, boxName:{0}, boxType:{1}, boxVersion:{2}", boxPkgName, boxPkgType, curBoxVersion);
        }
        // 3.判断目标app版本与目标box版本是否兼容
        isCompatible(targetAppPkgInfo, latestAppPkg, targetBoxPkgInfo, latestBoxPkg, compatibleCheckRes);
        return compatibleCheckRes;
    }

    /**
     * 判断app版本与box版本是否兼容
     *
     * @param targetAppPkg app版本
     * @param latestAppPkg 最新box版本
     * @param targetBoxPkg box版本
     * @param latestBoxPkg 最新box版本
     * @return 是否兼容
     */
    private void isCompatible(PkgInfoEntity targetAppPkg, PkgInfoEntity latestAppPkg, PkgInfoEntity targetBoxPkg,
                              PkgInfoEntity latestBoxPkg, CompatibleCheckRes compatibleCheckRes) {
        if (compatibleCheckRes.getIsAppForceUpdate() && compatibleCheckRes.getIsBoxForceUpdate()) {
            return;
        }
        // 1.检查目标app版本是否小于“目标box所兼容的最小app版本”
        String minCompatibleAppVersion;
        switch (PkgTypeEnum.fromValue(targetAppPkg.getPkgType())) {
            case ANDROID:
                minCompatibleAppVersion = targetBoxPkg.getMinCompatibleAndroidVersion();
                break;
            case IOS:
                minCompatibleAppVersion = targetBoxPkg.getMinCompatibleIOSVersion();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (!compatibleCheckRes.getIsAppForceUpdate() && isLowerVersion(targetAppPkg.getPkgVersion(), minCompatibleAppVersion)) {
            // 目标app版本比“目标box所兼容的最小app版本”低
            compatibleCheckRes.setIsAppForceUpdate(true);
            compatibleCheckRes.setLastestAppPkg(pkgInfoEntityToRes(latestAppPkg));
            LOG.infov("app version needs to force upgrade, appName:{0}, appType:{1}, appVersion:{2}", targetAppPkg.getPkgName(),
                    targetAppPkg.getPkgType(), targetAppPkg.getPkgVersion());

            //目标app版本=最新app版本,递归调用
            targetAppPkg = latestAppPkg;
            isCompatible(targetAppPkg, latestAppPkg, targetBoxPkg, latestBoxPkg, compatibleCheckRes);
        }

        // 2.检查目标box版本是否小于“目标app所兼容的最小box版本”
        String minCompatibleBoxVersion = targetAppPkg.getMinCompatibleBoxVersion();
        if (!compatibleCheckRes.getIsBoxForceUpdate() && isLowerVersion(targetBoxPkg.getPkgVersion(), minCompatibleBoxVersion)) {
            // 目标box的版本比“目标app所兼容的最小box版本”低
            compatibleCheckRes.setIsBoxForceUpdate(true);
            compatibleCheckRes.setLastestBoxPkg(pkgInfoEntityToRes(latestBoxPkg));
            LOG.infov("box version needs to force upgrade, boxName:{0}, boxType:{1}, boxVersion:{2}", targetBoxPkg.getPkgName(),
                    targetBoxPkg.getPkgType(), targetBoxPkg.getPkgVersion());

            //目标box版本=最新box版本,递归调用
            targetBoxPkg = latestBoxPkg;
            isCompatible(targetAppPkg, latestAppPkg, targetBoxPkg, latestBoxPkg, compatibleCheckRes);
        }
    }

    private PackageRes pkgInfoEntityToRes(PkgInfoEntity pkgInfoEntity) {
        return PackageRes.of(pkgInfoEntity.getPkgName(),
                pkgInfoEntity.getPkgType(),
                pkgInfoEntity.getPkgVersion(),
                pkgInfoEntity.getPkgSize(),
                pkgInfoEntity.getDownloadUrl(),
                pkgInfoEntity.getUpdateDesc(),
                pkgInfoEntity.getMd5(),
                pkgInfoEntity.getIsForceUpdate(),
                pkgInfoEntity.getMinCompatibleAndroidVersion(),
                pkgInfoEntity.getMinCompatibleIOSVersion(),
                pkgInfoEntity.getMinCompatibleBoxVersion());
    }

    private PkgInfoEntity pkgInfoReqToEntity(PackageReq packageReq) {
        String minBoxVersion = null;
        String minAndroidVersion = null;
        String minIOSVersion = null;
        switch (PkgTypeEnum.fromValue(packageReq.getPkgType())) {
            case ANDROID:
            case IOS:
                if (CommonUtils.isNullOrEmpty(packageReq.getMinBoxVersion())) {
                    minBoxVersion = applicationProperties.getMinBoxVersion();
                } else {
                    minBoxVersion = packageReq.getMinBoxVersion();
                }
                break;
            case BOX:
                if (CommonUtils.isNullOrEmpty(packageReq.getMinAndroidVersion())) {
                    minAndroidVersion = applicationProperties.getMinAndroidVersion();
                } else {
                    minAndroidVersion = packageReq.getMinAndroidVersion();
                }
                if (CommonUtils.isNullOrEmpty(packageReq.getMinIOSVersion())) {
                    minIOSVersion = applicationProperties.getMinIOSVersion();
                } else {
                    minIOSVersion = packageReq.getMinIOSVersion();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return PkgInfoEntity.of(packageReq.getPkgName(),
                packageReq.getPkgType(),
                packageReq.getPkgVersion(),
                packageReq.getPkgSize(),
                packageReq.getUpdateDesc(),
                packageReq.getIsForceUpdate() != null && packageReq.getIsForceUpdate(),
                packageReq.getDownloadUrl(),
                packageReq.getMd5(),
                minAndroidVersion,
                minIOSVersion,
                minBoxVersion, null);
    }

    public PackageRes getBoxLatestVersion(String boxName, String boxType) {
        PkgInfoEntity pkgInfoEntity = findByAppNameAndTypeSortedByVersion(boxName, boxType);
        if (pkgInfoEntity == null) {
            LOG.errorv("latest box version does not exist, boxName:{0}, boxType:{1}, not exist", boxName, boxType);
            throw new ServiceOperationException(ServiceError.LATEST_BOX_VERSION_NOT_EXIST);
        }
        return pkgInfoEntityToRes(pkgInfoEntity);
    }

    public PackageRes getAppLatestVersion(String appName, String appType) {
        PkgInfoEntity pkgInfoEntity = findByAppNameAndTypeSortedByVersion(appName, appType);
        if (pkgInfoEntity == null) {
            LOG.errorv("latest app version does not exist, appName:{0}, appType:{1}, not exist", appName, appType);
            throw new ServiceOperationException(ServiceError.LATEST_APP_VERSION_NOT_EXIST);
        }
        return pkgInfoEntityToRes(pkgInfoEntity);
    }

    /**
     * 当前版本是否具备更高的优先层级/版本号更高
     *
     * @param curVersion 当前版本
     * @param targetVersion 目标版本
     * @return 是否具备更高的优先层级
     */
    public boolean isHigherVersion(String curVersion, String targetVersion) {
        /**
         * 语义化版本 2.0.0
         * ref: https://semver.org/lang/zh-CN/
         */
        Version curV = Version.valueOf(curVersion);
        Version targetV = Version.valueOf(targetVersion);
        return curV.greaterThan(targetV);
    }

    public boolean isLowerVersion(String curVersion, String targetVersion) {
        Version curV = Version.valueOf(curVersion);
        Version targetV = Version.valueOf(targetVersion);
        return curV.lessThan(targetV);
    }

    public PkgInfoEntity findByAppNameAndTypeSortedByVersion(String pkgName, String pkgType) {
        List<PkgInfoEntity> pkgInfoEntities = pkgInfoEntityRepository.findByAppNameAndType(pkgName, pkgType);
        Optional<PkgInfoEntity> latestPkgInfo = pkgInfoEntities.stream().max(Comparator.comparing(entity -> Version.valueOf(entity.getPkgVersion())));
        return latestPkgInfo.orElse(null);
    }

    public PageListResult<PackageRes> listPackage(String pkgType, Integer currentPage, Integer pageSize) {
        List<PackageRes> pkgResList = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 1000;
        }
        // 查询列表
        List<PkgInfoEntity> pkgInfoEntityList = null;
        // 记录总数
        Long totalCount = 0L;
        if (CommonUtils.isNullOrEmpty(pkgType)) {
            pkgInfoEntityList = pkgInfoEntityRepository.findAll().page(currentPage - 1, pageSize).list();
            totalCount = pkgInfoEntityRepository.count();
        } else {
            // 根据类型查询
            pkgInfoEntityList = pkgInfoEntityRepository.findByAppType(pkgType, currentPage -1, pageSize);
            totalCount = pkgInfoEntityRepository.countByAppType(pkgType);
        }
        pkgInfoEntityList.forEach(pkgInfoEntity -> pkgResList.add(pkgInfoEntityToRes(pkgInfoEntity)));
        return PageListResult.of(pkgResList, PageInfo.of(totalCount, currentPage, pageSize));
    }

    @Transactional
    public void delPkginfos(List<Long> packageIds) {
        pkgInfoEntityRepository.deleteByPkgIds(packageIds);
    }
}
