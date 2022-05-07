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
		update("set applet_name=?1, applet_en_name=?11, state=?12, applet_id = ?2, applet_version=?3, applet_size=?4, update_desc=?5, icon_url=?6, down_url=?7," +
				"md5=?8, min_compatible_box_version=?9, updated_at = now(), force_update=?13 where applet_id=?10", appletInfoEntity.getName(), appletInfoEntity.getAppletId(),
				appletInfoEntity.getAppletVersion(), appletInfoEntity.getAppletSize(), appletInfoEntity.getUpdateDesc(), appletInfoEntity.getIconUrl(),
				appletInfoEntity.getDownUrl(), appletInfoEntity.getMd5(), appletInfoEntity.getMinCompatibleBoxVersion(), appletId, appletInfoEntity.getNameEn(),
				appletInfoEntity.getState(), appletInfoEntity.getIsForceUpdate());
	}
}
