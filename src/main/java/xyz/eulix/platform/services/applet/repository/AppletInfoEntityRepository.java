package xyz.eulix.platform.services.applet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.applet.entity.AppletInfoEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class AppletInfoEntityRepository implements PanacheRepository<AppletInfoEntity> {

	public AppletInfoEntity findByAppleId(String appletId) {
		return find("applet_id=?1", appletId).firstResult();
	}

	@Transactional
	public void delByAppletId(String appletId){
		delete("applet_id=?1", appletId);
	}

	@Transactional
	public void create(AppletInfoEntity appletInfoEntity){
		this.persist(appletInfoEntity);
	}

	@Transactional
	public void update(AppletInfoEntity appletInfoEntity, String appletId){
		update("set applet_name=?1, applet_en_name=?2, state=?3,  applet_version=?4, applet_size=?5, update_desc=?6, icon_url=?7, down_url=?8," +
				"md5=?9, min_compatible_box_version=?10, updated_at = now(), force_update=?11, categories=?12, web_md5=?13, web_down_url=?14," +
										"member_permission = ?15 where applet_id=?16", appletInfoEntity.getName(),
				appletInfoEntity.getNameEn(),appletInfoEntity.getState(), appletInfoEntity.getAppletVersion(), appletInfoEntity.getAppletSize(),
				appletInfoEntity.getUpdateDesc(),appletInfoEntity.getIconUrl(),appletInfoEntity.getDownUrl(),appletInfoEntity.getMd5(),
				 appletInfoEntity.getMinCompatibleBoxVersion(),appletInfoEntity.getIsForceUpdate(),appletInfoEntity.getCategories(),
						appletInfoEntity.getWebMd5(), appletInfoEntity.getWebDownUrl(), appletInfoEntity.getMemPermission(), appletId);
	}
}
