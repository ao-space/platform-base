package xyz.eulix.platform.services.helpcenter.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.helpcenter.entity.CatalogueEntity;
import xyz.eulix.platform.services.helpcenter.repository.CatalogueEntityRepository;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CatalogueService {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  CatalogueEntityRepository catalogueEntityRespository;

  public CatalogueEntity saveCatalogue(Long rootid, String name){
    if(!catalogueEntityRespository.findByCataName(name).isEmpty()){
      throw new ServiceOperationException(ServiceError.CATALOGUE_HAS_CREATE);
    }
    CatalogueEntity catalogueEntity = new CatalogueEntity();
    catalogueEntity.setCataName(name);
    catalogueEntity.setParentId(rootid);
    catalogueEntityRespository.create(catalogueEntity);
    return  catalogueEntity;
  }

  public CatalogueEntity updateCatalogue(Long rootid, String name) {
    catalogueEntityRespository.update(rootid, name);
    return catalogueEntityRespository.findById(rootid);
  }

  public List<CatalogueEntity> findByRootId(Long id){
    return  catalogueEntityRespository.find("parent_id", id).list();
  }

  public void deleteFromRootId(Long rootid){
    List<Long> childCatalogue = new ArrayList<>();
    findFromRootId(rootid, childCatalogue);
    for(Long id:childCatalogue){
      catalogueEntityRespository.deleteByNodeId(id);
    }
    catalogueEntityRespository.deleteByNodeId(rootid);
  }
  public void findFromRootId(Long id, List<Long> listRoot){
    List<CatalogueEntity>  list = findByRootId(id);
    if(list.size() == 0) {return ;}
    for (CatalogueEntity catalogueEntity:list){
     listRoot.add(catalogueEntity.getId());
     findFromRootId(catalogueEntity.getId(), listRoot);
    }
  }

  public void judePath(String[] list){
    if(list.length == 0 || !list[0].equals("1")) {throw new ServiceOperationException(ServiceError.CATALOGUE_NOT_EXIST);}
    int i = list.length - 1;
    if(catalogueEntityRespository.findById(Long.valueOf(list[i])) == null){
      throw new ServiceOperationException(ServiceError.CATALOGUE_NOT_EXIST);
    }
    while(i > 0){
      if(!catalogueEntityRespository.findById(Long.valueOf(list[i])).getParentId().equals(Long.valueOf(list[i-1]))){
        throw new ServiceOperationException(ServiceError.CATALOGUE_NOT_EXIST);
      }
      i=i-1;
    }
  }
}
