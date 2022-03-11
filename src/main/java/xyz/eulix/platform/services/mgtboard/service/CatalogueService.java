package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.CatalogueRes;
import xyz.eulix.platform.services.mgtboard.entity.CatalogueEntity;
import xyz.eulix.platform.services.mgtboard.repository.CatalogueEntityRepository;
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

    @Inject
    ArticleService articleService;

    public CatalogueRes saveCatalogue(Long rootid, String name){
        if(catalogueEntityRespository.findById(rootid) == null){
            throw new ServiceOperationException(ServiceError.CATALOGUE_NOT_EXIST);
        }
        if(!catalogueEntityRespository.findByCataName(name).isEmpty()){
            throw new ServiceOperationException(ServiceError.CATALOGUE_HAS_CREATE);
        }
        CatalogueEntity catalogueEntity = new CatalogueEntity();
        catalogueEntity.setCataName(name);
        catalogueEntity.setParentId(rootid);
        catalogueEntityRespository.create(catalogueEntity);
        return CatalogueRes.of(catalogueEntity.getId(), catalogueEntity.getCataName(),catalogueEntity.getParentId(),
                catalogueEntity.getCreatedAt(), catalogueEntity.getUpdatedAt());
    }

    public CatalogueRes updateCatalogue(Long rootid, String name) {
        if(rootid.equals(1L) ){
            throw new ServiceOperationException(ServiceError.CATALOGUE_IS_ROOT);
        }
        catalogueEntityRespository.update(rootid, name);
        CatalogueEntity catalogueEntity = catalogueEntityRespository.findById(rootid);
        return CatalogueRes.of(catalogueEntity.getId(), catalogueEntity.getCataName(),catalogueEntity.getParentId(),
                catalogueEntity.getCreatedAt(), catalogueEntity.getUpdatedAt());
    }

    public List<CatalogueRes> findByRootId(Long id){
        List<CatalogueRes> list =  new ArrayList<>();
        catalogueEntityRespository.find("parent_id", id).list().forEach(catalogueEntity ->
            list.add(CatalogueRes.of(catalogueEntity.getId(), catalogueEntity.getCataName(), catalogueEntity.getParentId(),
                    catalogueEntity.getCreatedAt(), catalogueEntity.getUpdatedAt())));
        return list;
    }

    public void deleteFromRootId(Long rootid){
        if(rootid.equals(1L) ){
            throw new ServiceOperationException(ServiceError.CATALOGUE_IS_ROOT);
        }
        List<Long> childCatalogueIds = new ArrayList<>();
        List<Long> articleIds = new ArrayList<>();
        findFromRootId(rootid, childCatalogueIds);
        childCatalogueIds.add(rootid);
        for(Long id:childCatalogueIds){
            articleIds.addAll(articleService.getArticleIdList(id));
        }
        articleService.deleteArticle(articleIds);
        catalogueEntityRespository.deleteByNodeIds(childCatalogueIds);;
    }
    public void findFromRootId(Long id, List<Long> listRoot){
        List<CatalogueRes>  list = findByRootId(id);
        if(list.size() == 0) {return ;}
        for (CatalogueRes catalogueRes:list){
            listRoot.add(catalogueRes.getId());
            findFromRootId(catalogueRes.getId(), listRoot);
        }
    }

}
