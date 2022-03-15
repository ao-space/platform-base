package xyz.eulix.platform.services.mgtboard.service;

import com.alibaba.excel.EasyExcel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.entity.ProposalEntity;
import xyz.eulix.platform.services.mgtboard.repository.ProposalEntityRepository;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.boundary.oss.OSSClient;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProposalService {
    private static final Logger LOG = Logger.getLogger("app.log");

    private static final Integer MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    @Inject
    ProposalEntityRepository proposalEntityRepository;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Inject
    ApplicationProperties properties;

    @Inject
    OSSClient ossClient;

    /**
     * 新增意见反馈
     *
     * @param proposalReq 意见反馈
     * @return 意见反馈
     */
    @Transactional
    public ProposalRes saveProposal(ProposalReq proposalReq) {
        // 校验userDomain是否存在
        if (!CommonUtils.isNullOrEmpty(proposalReq.getUserDomain())) {
            Optional<SubdomainEntity> subdomainEntityOp = subdomainEntityRepository.findByUserDomain(proposalReq.getUserDomain());
            if (subdomainEntityOp.isEmpty()) {
                LOG.warnv("userDomain does not exist, userDomain:{0}", proposalReq.getUserDomain());
                throw new ServiceOperationException(ServiceError.USER_DOMAIN_INVALID);
            }
        }
        ProposalEntity proposalEntity = proposalReqToEntity(proposalReq);
        proposalEntityRepository.persist(proposalEntity);
        return proposalEntityToRes(proposalEntity);
    }

    /**
     * 更新意见反馈
     *
     * @param proposalId  意见ID
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
                proposalReq.getPhoneNumber(), CommonUtils.isNullOrEmpty(proposalReq.getImageUrls()) ? null : String.join(",", proposalReq.getImageUrls()));
        return ProposalRes.of(proposalId,
                proposalEntity.getUserDomain(),
                proposalReq.getContent(),
                proposalReq.getEmail(),
                proposalReq.getPhoneNumber(),
                proposalReq.getImageUrls(),
                proposalEntity.getUpdatedAt());
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
     * @param pageSize    每页数量
     * @return 意见反馈列表
     */
    public PageListResult<ProposalRes> listProposal(Integer currentPage, Integer pageSize) {
        List<ProposalRes> proposalResList = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 1000;
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
                proposalEntity.getUserDomain(),
                proposalEntity.getContent(),
                proposalEntity.getEmail(),
                proposalEntity.getPhoneNumber(),
                CommonUtils.isNullOrEmpty(proposalEntity.getImageUrls()) ? null
                        : Arrays.asList(proposalEntity.getImageUrls().split(",")),
                proposalEntity.getUpdatedAt());
    }

    private ProposalEntity proposalReqToEntity(ProposalReq proposalReq) {
        return ProposalEntity.of(proposalReq.getUserDomain(),
                proposalReq.getContent(),
                proposalReq.getEmail(),
                proposalReq.getPhoneNumber(),
                CommonUtils.isNullOrEmpty(proposalReq.getImageUrls()) ? null
                        : String.join(",", proposalReq.getImageUrls()));
    }

    /**
     * 上传文件
     *
     * @param multipartBody 文件内容
     * @return 文件信息
     */
    public UploadFileRes upload(MultipartBody multipartBody, boolean isPublish) {
        if (multipartBody.file == null) {
            LOG.debug("input parameter file is null");
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "multipartBody.file");
        }
        String folderPath = appendSlash(isPublish ?properties.getOSSPublicPath():properties.getFileLocation()) + "/" + CommonUtils.getDayOrderFormat();
        initDirectory(folderPath);
        String filePath = folderPath + "/" + fileRename(multipartBody.fileName);
        File file = new File(filePath);
        try (InputStream inputStream = multipartBody.file;
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] b = new byte[2048];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, length);
                fileSizeCheck(file);
            }
        } catch (IOException e) {
            LOG.error("upload file failed, exception", e);
            throw new ServiceOperationException(ServiceError.UPLOAD_FILE_FAILED);
        }
        long fileSize = file.length();
        // 上传oss
        if(isPublish)
        {
            ossClient.fileUploadPublic(rmSlash(filePath), file);
        } else {
            ossClient.fileUpload(rmSlash(filePath), file);
        }
        // 删除本地文件
        boolean delOrNot = file.delete();
        if (!delOrNot) {
            LOG.warnv("delete local file:{0} failed", filePath);
        }
        return UploadFileRes.of(null, multipartBody.fileName, fileSize,
                isPublish?properties.getOSSPublicDomain()+appendSlash(filePath):filePath.substring(appendSlash(properties.getFileLocation()).length()));
    }

    public void delete(String filePath){
        if(filePath.startsWith(properties.getOSSPublicDomain())){
            ossClient.deleteObject(properties.getOSSBucketName(), filePath.substring(properties.getOSSPublicDomain().length()+1));
        }else{
            ossClient.deleteObject(properties.getOSSBucketName(), rmSlash(properties.getFileLocation()+filePath));
        }
    }

    private void fileSizeCheck(File file) {
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            LOG.errorv("file exceeds its maximum permitted size of {0}", MAX_FILE_SIZE_BYTES);
            throw new ServiceOperationException(ServiceError.FILE_SIZE_EXCEED_PERMIT, MAX_FILE_SIZE_BYTES);
        }
    }

    private void initDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            LOG.infov("Directory {0} does not exist, create it.", dir.getPath());
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                LOG.errorv("create directory:{0} failed", path);
                throw new ServiceOperationException(ServiceError.DIR_CREATE_FAILED);
            }
        }
    }

    private String fileRename(String fileName) {
        String newFileName = "Unkonwn";
        if (fileName != null) {
            int index = fileName.lastIndexOf(".");
            if (index != -1) {
                newFileName = CommonUtils.getUUID() + fileName.substring(index);
            } else {
                newFileName = CommonUtils.getUUID();
            }
        }
        return newFileName;
    }

    /**
     * 下载文件
     *
     * @param downloadFileReq 文件信息
     * @return Response
     */
    public Response download(DownloadFileReq downloadFileReq) {
        String filePath = downloadFileReq.getFileUrl();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String objectName = rmSlash(properties.getFileLocation()) + appendSlash(filePath);
        // 判断文件是否存在
        boolean found = ossClient.fileExistOrNot(objectName);
        if (!found) {
            LOG.errorv("file: {0} not found", filePath);
            throw new ServiceOperationException(ServiceError.FILE_NOT_FOUND);
        }
        Response.ResponseBuilder response = Response.ok(
                (StreamingOutput) output -> {
                    try (InputStream content = ossClient.fileDownload(objectName)) {
                        if (content != null) {
                            byte[] b = new byte[2048];
                            int length;
                            while ((length = content.read(b)) > 0) {
                                output.write(b, 0, length);
                            }
                        }
                    } catch (IOException e) {
                        LOG.error("download file failed, exception", e);
                        throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
                    }
                });
        response.header("Content-Disposition", "attachment;filename=" + fileName);
        return response.build();
    }

    /**
     * 导出文件
     * * @return Response
     */
    public Response export() {
        var dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = URLEncoder.encode("意见反馈-" + dateFormat.format(System.currentTimeMillis()) + ".xlsx",
                StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        List<ProposalEntity> allData = proposalEntityRepository.listAll();

        List<List<String>> lists = new ArrayList<>();
        for (ProposalEntity entity : allData) {
            List<String> list = new ArrayList<>();
            list.add(entity.getId() == null ? "" : String.valueOf(entity.getId()));
            list.add(entity.getUserDomain() == null ? "" : String.valueOf(entity.getUserDomain()));
            list.add(entity.getContent() == null ? "" : String.valueOf(entity.getContent()));
            list.add(entity.getEmail() == null ? "" : String.valueOf(entity.getEmail()));
            list.add(entity.getPhoneNumber() == null ? "" : String.valueOf(entity.getPhoneNumber()));
            list.add(entity.getImageUrls() == null ? "" : accessFileUrls(entity.getImageUrls()));
            list.add(entity.getCreatedAt() == null ? "" : String.valueOf(entity.getCreatedAt()));
            list.add(entity.getUpdatedAt() == null ? "" : String.valueOf(entity.getUpdatedAt()));
            list.add(entity.getVersion() == null ? "" : String.valueOf(entity.getVersion()));
            lists.add(list);
        }

        Response.ResponseBuilder response = Response.ok((StreamingOutput) output ->
                EasyExcel.write(output, ProposalEntity.class).sheet("sheet1").doWrite(lists));

        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.header("Content-Disposition", "attachment;filename=" + fileName);
        return response.build();
    }

    private String accessFileUrls(String fileUrls) {
        if (fileUrls == null) {
            return null;
        }
        List<String> urlList = Arrays.asList(fileUrls.split(","));
        List<String> accessUrlList = new ArrayList<>();
        urlList.forEach(url -> {
            String objectName = rmSlash(properties.getFileLocation()) + appendSlash(url);
            accessUrlList.add(ossClient.getFileUrl(objectName));
        });
        return String.join(",", accessUrlList);
    }

    private String appendSlash(String path) {
        if (path == null) {
            return null;
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String rmSlash(String path) {
        if (path == null) {
            return null;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
