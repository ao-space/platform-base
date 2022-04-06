package xyz.eulix.platform.services.mgtboard.service;

import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.ArticleRes;
import xyz.eulix.platform.services.mgtboard.entity.ArticleEntity;
import xyz.eulix.platform.services.mgtboard.repository.ArticleEntityRepository;
import xyz.eulix.platform.services.mgtboard.repository.CatalogueEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticleService {
	private static final Logger LOG = Logger.getLogger("app.log");
	@Inject
	CatalogueEntityRepository catalogueEntityRepository;

	@Inject
	ArticleEntityRepository articleEntityRepository;

	public ArticleRes createNewArticle(Long cataid, String title, String context, Integer state) {
		var articleEntity = new ArticleEntity();
		if (catalogueEntityRepository.findById(cataid) == null) {
			throw new ServiceOperationException(ServiceError.CATALOGUE_NOT_EXIST);
		}
		if (!articleEntityRepository.findByTitle(title).isEmpty()) {
			throw new ServiceOperationException(ServiceError.ARTICLE_HAS_CREATE);
		}
		articleEntity.setCataId(cataid);
		articleEntity.setContent(context);
		articleEntity.setTitle(title);
		articleEntity.setState(state);
		articleEntityRepository.create(articleEntity);
		return ArticleRes.of(articleEntity.getId(),
				articleEntity.getTitle(),
				articleEntity.getContent(),
				articleEntity.getCataId(),
				articleEntity.getState(),
				articleEntity.getPublishdAt(),
				articleEntity.getCreatedAt(),
				articleEntity.getUpdatedAt());
	}

	public ArticleRes updateArticle(Long articleId, Long cataId, String title, String context, Integer state) {
		var articleEntity = articleEntityRepository.findById(articleId);
		if (CommonUtils.isNull(articleEntity)) {
			throw new ServiceOperationException(ServiceError.ARTICLE_NOT_EXIST);
		}
		var articleEntityTitleCheckList = articleEntityRepository.findByTitle(title);
		if (!articleEntityTitleCheckList.isEmpty() && !articleEntityTitleCheckList.contains(articleEntity)) {
			throw new ServiceOperationException(ServiceError.ARTICLE_HAS_CREATE);
		}
		articleEntity = articleEntityRepository.findById(articleId);
		articleEntity.setCataId(cataId);
		articleEntity.setTitle(title);
		articleEntity.setContent(context);
		articleEntity.setState(state);
		articleEntityRepository.update(articleEntity);
		return ArticleRes.of(articleEntity.getId(),
				articleEntity.getTitle(),
				articleEntity.getContent(),
				articleEntity.getCataId(),
				articleEntity.getState(),
				articleEntity.getPublishdAt(),
				articleEntity.getCreatedAt(),
				articleEntity.getUpdatedAt());
	}

	public PageListResult<ArticleRes> getArticleList(Long cataId, Integer currentPage, Integer pageSize) {
		List<ArticleRes> articleResList = new ArrayList<>();
		if (currentPage == null || currentPage <= 0) {
			currentPage = 1;
		}
		if (pageSize == null) {
			pageSize = 1000;
		}
		List<ArticleEntity> articleEntityList = articleEntityRepository.findByCataIdAndSortBySortKey(Sort.by("updated_at").descending(), cataId, currentPage - 1, pageSize);
		articleEntityList.forEach(articleEntity -> articleResList.add(ArticleRes.of(articleEntity.getId(),
				articleEntity.getTitle(),
				null,
				articleEntity.getCataId(),
				articleEntity.getState(),
				articleEntity.getPublishdAt(),
				articleEntity.getCreatedAt(),
				articleEntity.getUpdatedAt())));
		return PageListResult.of(articleResList, PageInfo.of(articleResList.size(), currentPage, pageSize));
	}

	public List<Long> getArticleIdList(Long rootid) {
		List<ArticleEntity> articleEntities = articleEntityRepository.find("cata_id", rootid).list();
		List<Long> articleIdList = new ArrayList<>();
		articleEntities.forEach(articleEntity -> {
			articleIdList.add(articleEntity.getId());
		});
		return articleIdList;
	}

	public ArticleRes findByArticleId(Long id) {
		var articleEntity = articleEntityRepository.findById(id);
		if (CommonUtils.isNull(articleEntity)) {
			throw new ServiceOperationException(ServiceError.ARTICLE_NOT_EXIST);
		}
		return ArticleRes.of(articleEntity.getId(),
				articleEntity.getTitle(),
				articleEntity.getContent(),
				articleEntity.getCataId(),
				articleEntity.getState(),
				articleEntity.getPublishdAt(),
				articleEntity.getCreatedAt(),
				articleEntity.getUpdatedAt());
	}

	public void deleteArticle(List<Long> articleIds) {
		try {
			articleEntityRepository.deleteByIds(articleIds);
		} catch (Exception e) {
			throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
		}
	}

	@Transactional
	public void deleteArticle(Long articleId) {
		if (CommonUtils.isNull(articleEntityRepository.findById(articleId))) {
			throw new ServiceOperationException(ServiceError.ARTICLE_NOT_EXIST);
		}
		try {
			articleEntityRepository.deleteById(articleId);
		} catch (Exception e) {
			throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
		}
	}

}
