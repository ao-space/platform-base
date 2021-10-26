package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.entity.ProposalEntity;
import xyz.eulix.platform.services.mgtboard.repository.ProposalEntityRepository;
import xyz.eulix.platform.services.support.ResourceUtils;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ProposalService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ProposalEntityRepository proposalEntityRepository;

    /**
     * 新增意见反馈
     *
     * @param proposalReq 意见反馈
     * @return 意见反馈
     */
    @Transactional
    public ProposalRes saveProposal(ProposalReq proposalReq) {
        ProposalEntity proposalEntity = proposalReqToEntity(proposalReq);
        proposalEntityRepository.persist(proposalEntity);
        return proposalEntityToRes(proposalEntity);
    }

    /**
     * 更新意见反馈
     *
     * @param proposalId 意见ID
     * @param proposalReq 意见反馈
     * @return 意见反馈
     */
    @Transactional
    public ProposalRes updateProposal(Long proposalId, ProposalReq proposalReq) {
        ProposalEntity proposalEntity = proposalEntityRepository.findById(proposalId);
        if (proposalEntity == null) {
            LOG.warnv("proposal does not exist, proposalId:{0}", proposalId);
            throw new ServiceOperationException(ServiceError.PROPOSAL_NOT_EXIST);
        }
        proposalEntityRepository.updateById(proposalId, proposalReq.getContent(), proposalReq.getEmail(),
                proposalReq.getPhoneNumer(), String.join(",", proposalReq.getImageUrls()));
        return ProposalRes.of(proposalId,
                proposalReq.getContent(),
                proposalReq.getEmail(),
                proposalReq.getPhoneNumer(),
                proposalReq.getImageUrls());
    }

    /**
     * 删除意见反馈
     *
     * @param proposalId 意见ID
     */
    @Transactional
    public void deleteProposal(Long proposalId) {
        proposalEntityRepository.deleteById(proposalId);
    }

    /**
     * 查询意见反馈详情
     *
     * @param proposalId 意见ID
     * @return 意见反馈
     */
    public ProposalRes getProposal(Long proposalId) {
        ProposalEntity proposalEntity = proposalEntityRepository.findById(proposalId);
        if (proposalEntity == null) {
            LOG.warnv("proposal does not exist, proposalId:{0}", proposalId);
            throw new ServiceOperationException(ServiceError.PROPOSAL_NOT_EXIST);
        }
        return proposalEntityToRes(proposalEntity);
    }

    /**
     * 获取意见反馈列表
     *
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 意见反馈列表
     */
    public PageListResult<ProposalRes> listProposal(Integer currentPage, Integer pageSize) {
        List<ProposalRes> proposalResList = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        // 查询列表
        List<ProposalEntity> proposalEntities = proposalEntityRepository.findAll().page(currentPage - 1, pageSize).list();
        proposalEntities.forEach(proposalEntity -> {
            proposalResList.add(proposalEntityToRes(proposalEntity));
        });
        // 记录总数
        Long totalCount = proposalEntityRepository.count();
        return PageListResult.of(proposalResList, PageInfo.of(totalCount, currentPage, pageSize));
    }

    private ProposalRes proposalEntityToRes(ProposalEntity proposalEntity) {
        return ProposalRes.of(proposalEntity.getId(),
                proposalEntity.getContent(),
                proposalEntity.getEmail(),
                proposalEntity.getPhoneNumer(),
                Arrays.asList(proposalEntity.getImageUrls().split(",")));
    }

    private ProposalEntity proposalReqToEntity(ProposalReq proposalReq) {
        return ProposalEntity.of(proposalReq.getContent(),
                proposalReq.getEmail(),
                proposalReq.getPhoneNumer(),
                String.join(",", proposalReq.getImageUrls()));
    }

    /**
     * 上传文件
     *
     * @param multipartBody 文件内容
     * @return 文件信息
     */
    public UploadFileRes upload(MultipartBody multipartBody) {
        String imageFolder = "/data/forums/";
        String imagePath = imageFolder + multipartBody.fileName;
        try (FileOutputStream stream = new FileOutputStream(ResourceUtils.getFile(imagePath))) {
            byte[] b = new byte[2048];
            int length;
            while ((length = multipartBody.file.read(b)) > 0) {
                stream.write(b, 0, length);
            }
        } catch (Exception e) {
            LOG.error("upload file failed, exception", e);
            throw new ServiceOperationException(ServiceError.UPLOAD_FILE_FAILED);
        }
        return UploadFileRes.of(null, multipartBody.fileName, null, imagePath);
    }
}
