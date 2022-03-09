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
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public BoxInfosRes saveBoxInfos(BoxInfosReq boxInfosReq) {
        List<String> boxUUIDs = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        boxInfosReq.getBoxInfos().forEach(boxInfo -> {
            upsertBoxInfo(boxInfo.getBoxUUID(), boxInfo.getDesc(), boxInfo.getExtra(), boxUUIDs, failures);
        });
        return BoxInfosRes.of(boxUUIDs, failures);
    }

    @Transactional
    public <T> boolean upsertBoxInfo(String boxUUID, String desc, T extra, List<String> boxUUIDs, List<String> failures) {
        try {
            String extraStr = null;
            if (CommonUtils.isNotNull(extra)) {
                extraStr = operationUtils.objectToJson(extra);
            }
            if (boxInfoEntityRepository.findByBoxUUID(boxUUID).isPresent()) {
                boxInfoEntityRepository.updateByBoxUUID(extraStr, boxUUID);
            } else {
                BoxInfoEntity boxInfoEntity = new BoxInfoEntity();
                boxInfoEntity.setBoxUUID(boxUUID);
                boxInfoEntity.setDesc(desc);
                boxInfoEntity.setExtra(extraStr);
                boxInfoEntityRepository.createBoxInfo(boxInfoEntity);
            }
            boxUUIDs.add(boxUUID);
            return true;
        } catch (Exception e) {
            LOG.errorv(e, "box info save failed");
            failures.add(boxUUID);
            return false;
        }
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
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/boxTemplate.xlsx")){
            byte[] b = inputStream.readAllBytes();
            return Response.ok(b)
                    .header("Content-Disposition", "attachment;filename=" + URLEncoder.encode("出厂信息模板.xlsx", StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20"))
                    .header("Content-Length", b.length)
                    .build();
        } catch (IOException e) {
            LOG.error("download template failed, exception is:", e);
            throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
        }
    }

    public BoxInfosRes upload(MultipartBody multipartBody) {
        ArrayList<String> success = new ArrayList<>();
        ArrayList<BoxFailureInfo> fail = new ArrayList<>();
        EasyExcel.read(multipartBody.file, BoxExcelModel.class, new BoxExcelListener(this, operationUtils, success, fail)).sheet().doRead();
        return BoxInfosRes.of(success, fail);
    }

    public Response export(List<String> list){
        return  null;
    }
}
