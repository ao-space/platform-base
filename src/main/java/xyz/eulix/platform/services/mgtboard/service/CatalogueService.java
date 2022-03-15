package xyz.eulix.platform.services.mgtboard.service;

import io.quarkus.panache.common.Sort;
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

    private static final Long ROOT_ID = 1L;

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
        return CatalogueRes.of(catalogueEntity.getId(),
                catalogueEntity.getCataName(),
                catalogueEntity.getParentId(),
                catalogueEntity.getCreatedAt(),
                catalogueEntity.getUpdatedAt());
    }

    public CatalogueRes updateCatalogue(Long catatId, String name) {
        if(catatId.equals(ROOT_ID) ){
            throw new ServiceOperationException(ServiceError.CATALOGUE_IS_ROOT);
        }
        var cataEntityList = catalogueEntityRespository.findByCataName(name);
        CatalogueEntity catalogueEntity = catalogueEntityRespository.findById(catatId);
        if(!cataEntityList.isEmpty() && !cataEntityList.contains(catalogueEntity)) {throw new ServiceOperationException(ServiceError.CATALOGUE_HAS_CREATE);}
        catalogueEntityRespository.update(catatId, name);
        catalogueEntity = catalogueEntityRespository.findById(catatId);
        return CatalogueRes.of(catalogueEntity.getId(),
                catalogueEntity.getCataName(),
                catalogueEntity.getParentId(),
                catalogueEntity.getCreatedAt(),
                catalogueEntity.getUpdatedAt());
    }

    public List<CatalogueRes> findByRootId(Long id){
        List<CatalogueRes> list =  new ArrayList<>();
        catalogueEntityRespository.find("parent_id", Sort.by("created_at").descending(), id).list().forEach(catalogueEntity ->
            list.add(CatalogueRes.of(catalogueEntity.getId(),
                    catalogueEntity.getCataName(),
                    catalogueEntity.getParentId(),
                    catalogueEntity.getCreatedAt(),
                    catalogueEntity.getUpdatedAt())));
        return list;
    }

    public void deleteFromRootId(Long rootid){
        if(rootid.equals(ROOT_ID) ){
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
