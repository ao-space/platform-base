package xyz.eulix.platform.services.registry.service;

import org.jboss.logging.Logger;
import com.alibaba.excel.EasyExcel;
import xyz.eulix.platform.services.mgtboard.dto.MultipartBody;
import xyz.eulix.platform.services.registry.dto.registry.*;
import xyz.eulix.platform.services.registry.entity.*;
import xyz.eulix.platform.services.registry.repository.*;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.serialization.OperationUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class BoxInfoService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoEntityRepository boxInfoEntityRepository;

    @Inject
    OperationUtils operationUtils;

    public List<String> saveBoxInfos(BoxInfosReq boxInfosReq) {
        List<String> boxUUIDs = new ArrayList<>();
        boxInfosReq.getBoxInfos().forEach(boxInfo -> {
            try {
                saveBoxInfo(boxInfo);
                boxUUIDs.add(boxInfo.getBoxUUID());
            } catch (PersistenceException exception) {
                if (exception.getCause() != null && exception.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
                    LOG.infov("boxUUID:{0} already exists, skip...", boxInfo.getBoxUUID());
                } else {
                    LOG.errorv(exception, "box info save failed");
                    throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
                }
            }
        });
        return boxUUIDs;
    }

    @Transactional
    public void saveBoxInfo(BoxInfo boxInfo) {
        boxInfoEntityRepository.persist(boxInfoToEntity(boxInfo));
    }

    @Transactional
    public void delBoxInfos(List<String> boxUUIDs) {
        boxInfoEntityRepository.deleteByBoxUUIDS(boxUUIDs);
    }

    public PageListResult<BoxInfo> listBoxInfo(Integer currentPage, Integer pageSize) {
        List<BoxInfo> boxInfos = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 1000;
        }
        // 1.查询列表
        List<BoxInfoEntity> boxInfoEntities = boxInfoEntityRepository.findAll().page(currentPage - 1, pageSize).list();
        boxInfoEntities.forEach(boxInfoEntity -> boxInfos.add(entityToBoxInfo(boxInfoEntity)));
        // 2.记录总数
        Long totalCount = boxInfoEntityRepository.count();
        return PageListResult.of(boxInfos, PageInfo.of(totalCount, currentPage, pageSize));
    }

    private BoxInfoEntity boxInfoToEntity(BoxInfo boxInfo) {
        BoxInfoEntity boxInfoEntity = new BoxInfoEntity();
        {
            boxInfoEntity.setBoxUUID(boxInfo.getBoxUUID());
            boxInfoEntity.setDesc(boxInfo.getDesc());
            if (CommonUtils.isNotNull(boxInfo.getExtra())) {
                boxInfoEntity.setExtra(operationUtils.objectToJson(boxInfo.getExtra()));
            }
        }
        return boxInfoEntity;
    }

    private BoxInfo entityToBoxInfo(BoxInfoEntity boxInfoEntity) {
        BoxInfo boxInfo = new BoxInfo();
        {
            boxInfo.setBoxUUID(boxInfoEntity.getBoxUUID());
            boxInfo.setDesc(boxInfoEntity.getDesc());
            if (!CommonUtils.isNullOrEmpty(boxInfoEntity.getExtra())) {
                boxInfo.setExtra(operationUtils.jsonToObject(boxInfoEntity.getExtra(), Object.class));
            }
        }
        return boxInfo;
    }

    public boolean isValidBoxUUID(String boxUUID) {
        Optional<BoxInfoEntity> boxInfoEntityOp = boxInfoEntityRepository.findByBoxUUID(boxUUID);
        return boxInfoEntityOp.isPresent();
    }

    public Response template() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/boxTemplate.xlsx")) {
            return Response.ok(inputStream)
                    .header("Content-Disposition", "attachment;filename=" + URLEncoder.encode("出厂信息模板.xlsx", StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20"))
                    .header("Content-Length", inputStream != null ? inputStream.available() : 0)
                    .build();
        } catch (IOException e) {
            LOG.error("download template failed, exception is:", e);
            throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
        }
    }

    public BoxInfosRes upload(MultipartBody multipartBody) {
        ArrayList<String> success = new ArrayList<>();
        ArrayList<String> fail = new ArrayList<>();
        EasyExcel.read(multipartBody.file, BoxExcelModel.class, new BoxExcelListener(operationUtils, boxInfoEntityRepository, success, fail)).sheet().doRead();
        return BoxInfosRes.of(success, fail);
    }
}
