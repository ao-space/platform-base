package xyz.eulix.platform.services.applet.service;

import com.github.zafarkhaja.semver.Version;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.applet.dto.AppletInfoRes;
import xyz.eulix.platform.services.applet.dto.AppletPostReq;
import xyz.eulix.platform.services.applet.dto.AppletReq;
import xyz.eulix.platform.services.applet.dto.CheckAppletResult;
import xyz.eulix.platform.services.applet.entity.AppletInfoEntity;
import xyz.eulix.platform.services.applet.repository.AppletInfoEntityRepository;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.entity.RegistryClientEntity;
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
					appletInfoEntity.getAppletId(), appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
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
					appletInfoEntity.getAppletId(), appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
					appletInfoEntity.getIconUrl(), appletInfoEntity.getUpdateDesc(),
					appletInfoEntity.getIsForceUpdate(),appletInfoEntity.getUpdatedAt()));
		}
		return resp;
	}

	public AppletInfoRes saveApplet(AppletPostReq appletPostReq){
		if(CommonUtils.isNotNull(appletInfoEntityRepository.findByAppleId(appletPostReq.getAppletId()))){
			throw new ServiceOperationException(ServiceError.DUPPLICATE_APPLET);
		}
		appletPostReq.setIsForceUpdate(appletPostReq.getIsForceUpdate() == null?false : appletPostReq.getIsForceUpdate());
		var appletInfoEntity = new AppletInfoEntity();
		appletInfoEntity.setState(appletPostReq.getState()==null?0: appletPostReq.getState());
		appletPostReqToEntity(appletPostReq, appletInfoEntity);
		appletInfoEntityRepository.create(appletInfoEntity);
		return AppletInfoRes.of(appletInfoEntity.getName(),
				                appletInfoEntity.getNameEn() == null? "":appletInfoEntity.getNameEn(),
				                appletInfoEntity.getState(),
				                appletInfoEntity.getAppletId(), appletInfoEntity.getMd5(),appletInfoEntity.getAppletVersion(),
				                appletInfoEntity.getIconUrl(),  appletInfoEntity.getUpdateDesc(),
				                appletInfoEntity.getIsForceUpdate(), appletInfoEntity.getUpdatedAt());
	}

	public AppletInfoRes updateApplet(String appletId, AppletPostReq appletPostReq){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletId);
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		if(!appletPostReq.getAppletId().equals(appletId) && CommonUtils.isNotNull(appletInfoEntityRepository.findByAppleId(appletPostReq.getAppletId())) ){
			throw new ServiceOperationException(ServiceError.DUPPLICATE_APPLET);
		}
		appletPostReq.setIsForceUpdate(appletPostReq.getIsForceUpdate() == null?false : appletPostReq.getIsForceUpdate());
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
		appletInfoEntity.setAppletSize(appletPostReq.getAppletSize());
		appletInfoEntity.setAppletVersion(appletPostReq.getAppletVersion());
		appletInfoEntity.setAppletSecret(appletPostReq.getAppletSecret()!=null?appletPostReq.getAppletSecret():appletInfoEntity.getAppletSecret());
		appletInfoEntity.setAppletId(appletPostReq.getAppletId());
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
		return utils.downLoadFile(appletInfoEntity.getDownUrl());
	}

	public void appletDelete(String appletId){
		var appletInfoEntity = appletInfoEntityRepository.findByAppleId(appletId);
		if(CommonUtils.isNull(appletInfoEntity)) {
			throw new ServiceOperationException(ServiceError.APPLET_NOT_EXIST);
		}
		appletInfoEntityRepository.delByAppletId(appletId);
	}
	public CheckAppletResult checkApplet(String boxRegKey, String id, String secret) {
		Optional<RegistryBoxEntity> boxEntityOp = registryBoxEntityRepository.find("box_reg_key=?1", boxRegKey).firstResultOptional();
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
