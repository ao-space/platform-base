package xyz.eulix.platform.services.mgtboard.service;

import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.entity.ReservedDomainEntity;
import xyz.eulix.platform.services.mgtboard.repository.ReservedDomainEntityRepository;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
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
public class ReservedDomainService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ReservedDomainEntityRepository reservedDomainEntityRepository;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    // 创建保留域名
    @Transactional
    public ReservedDomainCreateRsp create(ReservedDomainCreateReq req) {

        ReservedDomainEntity entity = ReservedDomainEntity.of(req.getRegex(), req.getDesc());
        reservedDomainEntityRepository.persist(entity);
        ReservedDomainCreateRsp rsp = ReservedDomainCreateRsp.of(entity.getId());
        return rsp;
    }

    // 删除保留域名
    @Transactional
    public ReservedDomainDeleteRsp delete(List<Long> regexIds) {
        Long deletedCount = reservedDomainEntityRepository.deleteByRegexIds(regexIds);
        ReservedDomainDeleteRsp rsp = ReservedDomainDeleteRsp.of(deletedCount);
        return rsp;
    }

    // 更新保留域名
    @Transactional
    public ReservedDomainUpdateRsp update(Long regexId, ReservedDomainUpdateReq req) {

        ReservedDomainEntity entity = ReservedDomainEntity.of(req.getRegex(), req.getDesc());
        Long updatedCount = reservedDomainEntityRepository.updateByRegexId(regexId, entity);
        ReservedDomainUpdateRsp rsp = ReservedDomainUpdateRsp.of(updatedCount);
        return rsp;
    }

    // 查询保留域名. 分页返回结果.
    public PageListResult<ReservedDomainInfo> queryReservedDomain(Integer currentPage, Integer pageSize) {
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 1000;
        }

        List<ReservedDomainEntity> boxInfoEntities = reservedDomainEntityRepository.findAll(Sort.by("updated_at").descending()).page(currentPage - 1, pageSize).list();

        Long totalCount = reservedDomainEntityRepository.count();

        List<ReservedDomainInfo> reservedDomainInfoList = new ArrayList<>();
        boxInfoEntities.forEach(entity -> reservedDomainInfoList.add(reservedDomainEntityToInfo(entity)));
        return PageListResult.of(reservedDomainInfoList, PageInfo.of(totalCount, currentPage, pageSize));
    }

    // 查询保留域名结果转换
    private ReservedDomainInfo reservedDomainEntityToInfo(ReservedDomainEntity entity) {
        return ReservedDomainInfo.of(entity.getId(),entity.getRegex(), entity.getDescription(), entity.getUpdatedAt());
    }

    // 查询匹配正则的用户域名. 不分页.
    public List<ReservedDomainMatchInfo> queryMatchInfo(Long regexId) {
        List<ReservedDomainMatchInfo> matchInfo = new ArrayList<ReservedDomainMatchInfo>();
        ReservedDomainEntity entity = reservedDomainEntityRepository.findById(regexId);
        if (entity.getRegex().isEmpty()) {
            LOG.warnv("entity.getRegex is empty, regexId:{0}", regexId);
            throw new ServiceOperationException(ServiceError.RESERVED_DOMAIN_LENGTH_ERROR);
        }

        List<SubdomainEntity> subdomainEntities = subdomainEntityRepository.findByRegularExpression(entity.getRegex());
        subdomainEntities.forEach(subDomain -> matchInfo.add(subdomainEntityToMatchInfo(subDomain)));
        return matchInfo;
    }

    // 查询匹配域名结果转换.
    private ReservedDomainMatchInfo subdomainEntityToMatchInfo(SubdomainEntity subDomain) {
        return ReservedDomainMatchInfo.of(subDomain.getUserDomain(), subDomain.getUpdatedAt());
    }


}
