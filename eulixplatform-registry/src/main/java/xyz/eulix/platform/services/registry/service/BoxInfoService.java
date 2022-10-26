package xyz.eulix.platform.services.registry.service;

import com.alibaba.excel.EasyExcel;
import javax.validation.constraints.NotBlank;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.registry.dto.registry.MultipartBody;
import xyz.eulix.platform.services.registry.dto.registry.BoxFailureInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfosReq;
import xyz.eulix.platform.services.registry.dto.registry.BoxInfosRes;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.services.registry.entity.BoxExcelModel;
import xyz.eulix.platform.services.registry.entity.BoxExcelModelV2;
import xyz.eulix.platform.services.registry.entity.BoxInfoEntity;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;
import xyz.eulix.platform.services.registry.entity.RegistryBoxEntity;
import xyz.eulix.platform.services.registry.repository.BoxInfoEntityRepository;
import xyz.eulix.platform.services.registry.repository.RegistryBoxEntityRepository;
import xyz.eulix.platform.common.support.model.PageInfo;
import xyz.eulix.platform.common.support.model.PageListResult;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides box and client registry service.
 */
@ApplicationScoped
public class BoxInfoService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    BoxInfoEntityRepository boxInfoEntityRepository;

    @Inject
    RegistryBoxEntityRepository registryBoxEntityRepository;

    @Inject
    OperationUtils operationUtils;

    public BoxInfosRes<String> saveBoxInfos(BoxInfosReq boxInfosReq) {
        List<String> boxUUIDs = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        boxInfosReq.getBoxInfos().forEach(boxInfo -> upsertBoxInfo(boxInfo.getBoxUUID(), boxInfo.getDesc(), boxInfo.getExtra(), boxUUIDs, failures));
        return BoxInfosRes.of(boxUUIDs, failures);
    }

    public BoxInfosRes<String> saveBoxInfosV2(BoxInfosReq boxInfosReq) {
        List<String> boxUUIDs = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        boxInfosReq.getBoxInfos().forEach(boxInfo -> upsertBoxInfoV2(boxInfo.getBoxUUID(), boxInfo.getDesc(), boxInfo.getExtra(), boxInfo.getAuthType(), boxInfo.getBoxPubKey(), boxUUIDs, failures));
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
                boxInfoEntityRepository.updateByBoxUUID(extraStr, AuthTypeEnum.BOX_UUID.getName(), null, boxUUID);
            } else {
                BoxInfoEntity boxInfoEntity = new BoxInfoEntity();
                boxInfoEntity.setBoxUUID(boxUUID);
                boxInfoEntity.setDesc(desc);
                boxInfoEntity.setExtra(extraStr);
                boxInfoEntity.setAuthType(AuthTypeEnum.BOX_UUID.getName());
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
    public <T> boolean upsertBoxInfoV2(String boxUUID, String desc, T extra, String authType, String boxPubKey,List<String> boxUUIDs, List<String> failures) {
        try {
            isValidBoxInfo(authType, boxPubKey);

            String extraStr = null;
            if (CommonUtils.isNotNull(extra)) {
                extraStr = operationUtils.objectToJson(extra);
            }
            if (boxInfoEntityRepository.findByBoxUUID(boxUUID).isPresent()) {
                boxInfoEntityRepository.updateByBoxUUID(extraStr, authType, boxPubKey, boxUUID);
            } else {
                BoxInfoEntity boxInfoEntity = new BoxInfoEntity();
                boxInfoEntity.setBoxUUID(boxUUID);
                boxInfoEntity.setDesc(desc);
                boxInfoEntity.setExtra(extraStr);
                boxInfoEntity.setBoxPubKey(boxPubKey);
                boxInfoEntity.setAuthType(authType);
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
    public Long delBoxInfos(List<String> boxUUIDs) {
        return boxInfoEntityRepository.deleteByBoxUUIDS(boxUUIDs);
    }

    public PageListResult<BoxInfo> listBoxInfo(Integer currentPage, Integer pageSize) {
        List<BoxInfo> boxInfos = new ArrayList<>();
        List<String> boxUUIDs = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 2000;
        }
        // 1.查询列表
        List<BoxInfoEntity> boxInfoEntities = boxInfoEntityRepository.findAll().page(currentPage - 1, pageSize).list();
        boxInfoEntities.forEach(boxInfoEntity -> {
            boxUUIDs.add(boxInfoEntity.getBoxUUID());
        });
        // 2.查询是否已注册
        List<RegistryBoxEntity> registryBoxEntities = registryBoxEntityRepository.findByBoxUUIDs(boxUUIDs);
        Map<String, RegistryBoxEntity> boxEntityMap = registryBoxEntities.stream().collect(Collectors.toMap(RegistryBoxEntity::getBoxUUID, entity -> entity));
        boxInfoEntities.forEach(boxInfoEntity -> {
            BoxInfo boxInfo = entityToBoxInfo(boxInfoEntity);
            if (boxEntityMap.containsKey(boxInfoEntity.getBoxUUID())) {
                boxInfo.setRegistered(true);
            }
            boxInfos.add(boxInfo);
        });
        // 3.记录总数
        Long totalCount = boxInfoEntityRepository.count();
        return PageListResult.of(boxInfos, PageInfo.of(totalCount, currentPage, pageSize));
    }

    public PageListResult<BoxInfo> listBoxInfo(Integer currentPage, Integer pageSize, Boolean isRegistry) {
        List<BoxInfo> boxInfos = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 2000;
        }
        List<BoxInfoEntity> boxInEntities = boxInfoEntityRepository.findWithBoxRegistries(isRegistry).page(currentPage - 1, pageSize).list();
        boxInEntities.forEach(boxInfoEntity -> {
            BoxInfo boxInfo = entityToBoxInfo(boxInfoEntity);
            boxInfo.setRegistered(isRegistry.booleanValue());
            boxInfos.add(boxInfo);
        });
        return PageListResult.of(boxInfos, PageInfo.of(boxInfoEntityRepository.getWithBoxRegistriesCount(isRegistry), currentPage, pageSize));
    }

    public PageListResult<BoxInfo> findBoxByBoxUUID(String boxUUID) {
        Optional<BoxInfoEntity> boxInfoEntityOP = boxInfoEntityRepository.findByBoxUUID(boxUUID);
        List<BoxInfo> boxInfos = new ArrayList<>();
        if (boxInfoEntityOP.isEmpty()) {
            return PageListResult.of(boxInfos, PageInfo.of(0L, 1, 1));
        }
        BoxInfo boxInfo = entityToBoxInfo(boxInfoEntityOP.get());
        if (registryBoxEntityRepository.findByBoxUUID(boxUUID).isPresent()) {
            boxInfo.setRegistered(true);
        } else {
            boxInfo.setRegistered(false);
        }
        boxInfos.add(boxInfo);
        return PageListResult.of(boxInfos, PageInfo.of(1L, 1, 1));
    }

    private BoxInfo entityToBoxInfo(BoxInfoEntity boxInfoEntity) {
        BoxInfo boxInfo = new BoxInfo();
        {
            boxInfo.setBoxUUID(boxInfoEntity.getBoxUUID());
            boxInfo.setDesc(boxInfoEntity.getDesc());
            boxInfo.setUpdatedAt(boxInfoEntity.getUpdatedAt());
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

    public Response template(String version) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/boxTemplate"+version+".xlsx")) {
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

    public BoxInfosRes<BoxFailureInfo> upload(MultipartBody multipartBody) {
        ArrayList<String> success = new ArrayList<>();
        ArrayList<String> failure = new ArrayList<>();
        ArrayList<BoxFailureInfo> fail = new ArrayList<>();
        EasyExcel.read(multipartBody.file, BoxExcelModel.class, new BoxExcelListener(this, operationUtils, success, failure, fail)).sheet().doRead();
        return BoxInfosRes.of(success, fail);
    }

    public BoxInfosRes<BoxFailureInfo> uploadV2(MultipartBody multipartBody) {
        ArrayList<String> success = new ArrayList<>();
        ArrayList<String> failure = new ArrayList<>();
        ArrayList<BoxFailureInfo> fail = new ArrayList<>();
        EasyExcel.read(multipartBody.file, BoxExcelModelV2.class, new BoxExcelListenerV2(this, operationUtils, success, failure, fail)).sheet().doRead();
        return BoxInfosRes.of(success, fail);
    }

    public Response export(List<BoxInfo> boxInfosReq) {
        if (boxInfosReq.isEmpty()) {
            throw new ServiceOperationException(ServiceError.BOXUUIDS_IS_EMPTY);
        }
        var dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = URLEncoder.encode("盒子信息-" + dateFormat.format(System.currentTimeMillis()) + ".xlsx",
                StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        List<BoxExcelModel> lists = new ArrayList<>();
        List<BoxInfoEntity> entities = boxInfoEntityRepository.findByBoxUUIDS(this.boxInfosToBoxUUIDs(boxInfosReq));
        for (BoxInfoEntity boxInfoEntity : entities) {
            lists.add(operationUtils.jsonToObject(boxInfoEntity.getExtra(), BoxExcelModel.class));
        }
        Response.ResponseBuilder response = Response.ok((StreamingOutput) output ->
                EasyExcel.write(output, BoxExcelModel.class).sheet("sheet1").doWrite(lists));
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.header("Content-Disposition", "attachment;filename=" + fileName);
        return response.build();
    }

    public Response exportV2(List<String> boxUUIDs) {
        var dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = URLEncoder.encode("盒子信息-" + dateFormat.format(System.currentTimeMillis()) + ".xlsx",
            StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        List<BoxExcelModelV2> lists = new ArrayList<>();
        List<BoxInfoEntity> entities = boxInfoEntityRepository.findByBoxUUIDS(boxUUIDs);
        for (BoxInfoEntity boxInfoEntity : entities) {
            var boxExcelModelV2 = operationUtils.jsonToObject(boxInfoEntity.getExtra(),
                BoxExcelModelV2.class);
            boxExcelModelV2.setAuthType(boxInfoEntity.getAuthType());
            boxExcelModelV2.setBoxPubKey(boxInfoEntity.getBoxPubKey());
            lists.add(boxExcelModelV2);
        }
        Response.ResponseBuilder response = Response.ok((StreamingOutput) output ->
            EasyExcel.write(output, BoxExcelModelV2.class).sheet("sheet1").doWrite(lists));
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.header("Content-Disposition", "attachment;filename=" + fileName);
        return response.build();
    }

    public List<String> boxInfosToBoxUUIDs(List<BoxInfo> boxInfos) {
        List<String> boxUUIDs = new ArrayList<>();
        boxInfos.forEach(boxInfo -> boxUUIDs.add(boxInfo.getBoxUUID()));
        return boxUUIDs;
    }

    /**
     * 校验入参是否合法
     *
     * @param authType 认证类型
     * @param boxPubKey 盒子公钥
     */
    public void isValidBoxInfo(String authType, String boxPubKey) {
        if (CommonUtils.isNullOrEmpty(authType)) {
            LOG.errorv("authType is null");
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "authType");
        }
        if (AuthTypeEnum.BOX_PUB_KEY.getName().equals(authType) && CommonUtils.isNullOrEmpty(boxPubKey)) {
            LOG.errorv("boxPubKey is null");
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "boxPubKey");
        }
    }
}
