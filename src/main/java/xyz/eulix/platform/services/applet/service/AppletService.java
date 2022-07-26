package xyz.eulix.platform.services.applet.service;

import com.github.zafarkhaja.semver.Version;
import xyz.eulix.platform.services.applet.dto.*;
import xyz.eulix.platform.services.applet.entity.AppletInfoEntity;
import xyz.eulix.platform.services.applet.repository.AppletInfoEntityRepository;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.repository.RegistryBoxEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.serialization.OperationUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class AppletService {
	@Inject
	AppletInfoEntityRepository appletInfoEntityRepository;

	@Inject
	RegistryBoxEntityRepository registryBoxEntityRepository;

	@Inject
	OperationUtils utils;

	public List<AppletInfoRes> getAppletInfo(){
		Iterator<AppletInfoEntity> appletList = appletInfoEntityRepository.findAll().stream().iterator();
		List<AppletInfoRes> resp = new ArrayList<>();
		while(appletList.hasNext()){
			var appletInfoEntity = appletList.next();
			resp.add(AppletInfoRes.of(appletInfoEntity.getName(),
					appletInfoEntity.getNameEn() == null?"":appletInfoEntity.getNameEn(),
					appletInfoEntity.getState(),
					appletInfoEntity.getAppletId(),  appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
					appletInfoEntity.getIconUrl(), appletInfoEntity.getUpdateDesc(),
					appletInfoEntity.getIsForceUpdate(),appletInfoEntity.getUpdatedAt()));
		}
		return resp;
	}

	public List<AppletInfoRes> getAppletInfo(String appletId){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletId);
		List<AppletInfoRes> resp = new ArrayList<>();
		if(appletInfoEntity != null) {
			resp.add(AppletInfoRes.of(appletInfoEntity.getName(),
					appletInfoEntity.getNameEn() == null?"":appletInfoEntity.getNameEn(),
					appletInfoEntity.getState(),
					appletInfoEntity.getAppletId(),  appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
					appletInfoEntity.getIconUrl(), appletInfoEntity.getUpdateDesc(),
					appletInfoEntity.getIsForceUpdate(),appletInfoEntity.getUpdatedAt()));
		}
		return resp;
	}

	public AppletRegistryRes saveApplet(AppletRegistryInfo appletRegistryInfo){
        String appletId = CommonUtils.unifiedRandomHexCharters(16);
        int num = 10; // 自主生成唯一性appletId，可重复尝试10次
        while(appletInfoEntityRepository.findByAppleId(appletId) != null && num > 1) {
			appletId = CommonUtils.createUnifiedRandomCharacters(16);
			num = num -1;
		}
        String appletSecret = CommonUtils.createUnifiedRandomCharacters(64);
		var appletInfoEntity = new AppletInfoEntity();
		appletInfoEntity.setAppletId(appletId);
		appletInfoEntity.setAppletSecret(appletSecret);
		appletInfoEntity.setState(appletRegistryInfo.getState()==null?1: appletRegistryInfo.getState());
		appletInfoEntity.setIsForceUpdate(appletRegistryInfo.getIsForceUpdate() == null?false : appletRegistryInfo.getIsForceUpdate());
		appletInfoEntity.setCategories(appletRegistryInfo.getCategories());
		appletInfoEntity.setName(appletRegistryInfo.getAppletName());
		appletInfoEntity.setNameEn(appletRegistryInfo.getAppletNameEn());
		appletInfoEntity.setAppletVersion(appletRegistryInfo.getAppletVersion());
		appletInfoEntity.setMinCompatibleBoxVersion(CommonUtils.isNullOrEmpty(appletRegistryInfo.getMinCompatibleBoxVersion())?
				                                             "0.0.0" : appletRegistryInfo.getMinCompatibleBoxVersion());
		appletInfoEntity.setUpdateDesc(appletRegistryInfo.getUpdateDesc());
		appletInfoEntity.setIconUrl(appletRegistryInfo.getIconUrl() !=null?appletRegistryInfo.getIconUrl():"");
		appletInfoEntity.setDownUrl(appletRegistryInfo.getDownUrl() != null?appletRegistryInfo.getDownUrl():"");
		appletInfoEntityRepository.create(appletInfoEntity);
		return AppletRegistryRes.of(appletId,appletSecret);
	}

	public AppletInfoRes updateApplet(String appletId, AppletPostReq appletPostReq){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletId);
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		appletPostReq.setIsForceUpdate(appletPostReq.getIsForceUpdate() == null?appletInfoEntity.getIsForceUpdate() : appletPostReq.getIsForceUpdate());
		appletPostReqToEntity(appletPostReq, appletInfoEntity);
		appletInfoEntity.setUpdatedAt(OffsetDateTime.now());
		appletInfoEntityRepository.update(appletInfoEntity, appletId);
		return  AppletInfoRes.of(appletInfoEntity.getName(),
				appletInfoEntity.getNameEn() == null?"":appletInfoEntity.getNameEn(),
				appletInfoEntity.getState(),
				appletInfoEntity.getAppletId(), appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
				appletInfoEntity.getIconUrl(),  appletInfoEntity.getUpdateDesc(),
				appletInfoEntity.getIsForceUpdate(),appletInfoEntity.getUpdatedAt());
	}

	public void appletPostReqToEntity(AppletPostReq appletPostReq, AppletInfoEntity appletInfoEntity){
		appletInfoEntity.setName(appletPostReq.getAppletName() != null ? appletPostReq.getAppletName() : appletInfoEntity.getName());
		appletInfoEntity.setNameEn(appletPostReq.getAppletNameEn()!= null ? appletPostReq.getAppletNameEn():appletInfoEntity.getNameEn());
		appletInfoEntity.setState(appletPostReq.getState() != null?appletPostReq.getState():appletInfoEntity.getState());
		appletInfoEntity.setAppletSize(appletPostReq.getAppletSize() != null?appletPostReq.getAppletSize(): appletInfoEntity.getAppletSize());
		appletInfoEntity.setAppletVersion(appletPostReq.getAppletVersion()!= null ? appletPostReq.getAppletVersion() : appletInfoEntity.getAppletVersion());
		appletInfoEntity.setDownUrl(appletPostReq.getDownUrl()!=null? appletPostReq.getDownUrl() : appletInfoEntity.getDownUrl());
		appletInfoEntity.setIconUrl(appletPostReq.getIconUrl()!= null ? appletPostReq.getIconUrl() : appletInfoEntity.getIconUrl());
		appletInfoEntity.setCategories(appletPostReq.getCategories()!= null ? appletPostReq.getCategories() : appletInfoEntity.getCategories());
		appletInfoEntity.setMd5(appletPostReq.getMd5());
		appletInfoEntity.setUpdateDesc(appletPostReq.getUpdateDesc()!= null? appletPostReq.getUpdateDesc() : appletInfoEntity.getUpdateDesc());
		appletInfoEntity.setIsForceUpdate(appletPostReq.getIsForceUpdate()!=null?appletPostReq.getIsForceUpdate() : appletInfoEntity.getIsForceUpdate());
		appletInfoEntity.setMinCompatibleBoxVersion(appletPostReq.getMinCompatibleBoxVersion()!=null?appletPostReq.getMinCompatibleBoxVersion() : appletInfoEntity.getMinCompatibleBoxVersion());
	}

	public boolean compatiableCheck(AppletReq appletReq){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletReq.getAppletId());
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		if(Version.valueOf(appletReq.getCurBoxVersion()).lessThan(Version.valueOf(appletInfoEntity.getMinCompatibleBoxVersion()))){
			return false;
		}
		return true;
	}

	public Response downAppletPackage(AppletReq appletReq){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletReq.getAppletId());
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		if(CommonUtils.isNullOrEmpty(appletInfoEntity.getDownUrl())) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		return utils.downLoadFile(appletInfoEntity.getDownUrl());
	}

	public void appletDelete(String appletId){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletId);
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		appletInfoEntityRepository.delByAppletId(appletId);
	}
	public CheckAppletResult checkApplet(String boxUUID, String boxRegKey, String id, String secret) {
		Optional<RegistryBoxEntity> boxEntityOp = registryBoxEntityRepository.findByBoxUUIDAndBoxRegKey(boxUUID, boxRegKey);
		if(boxEntityOp.isEmpty()) {
			return CheckAppletResult.of(false, null,null);
		}
		Optional<AppletInfoEntity> appletInfoOP =  appletInfoEntityRepository.find("applet_id=?1 and applet_secret=?2", id, secret).firstResultOptional();
		if(appletInfoOP.isPresent()) {
			var appletInfo = appletInfoOP.get();
			return CheckAppletResult.of(true, Arrays.asList(appletInfo.getCategories().split(",")), CheckAppletResult.AppletInfo.of(appletInfo.getName(),
					appletInfo.getUpdateDesc(), appletInfo.getIconUrl()));
		} else {
			return CheckAppletResult.of(false, null,null);
		}
	}
}
