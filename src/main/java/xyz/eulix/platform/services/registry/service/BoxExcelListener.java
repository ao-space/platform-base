package xyz.eulix.platform.services.registry.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.registry.entity.BoxExcelModel;
import xyz.eulix.platform.services.registry.entity.BoxInfoEntity;
import xyz.eulix.platform.services.registry.repository.BoxInfoEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;

public class BoxExcelListener implements ReadListener<BoxExcelModel> {
  private static final Logger LOG = Logger.getLogger("app.log");

  private BoxInfoEntityRepository boxInfoEntityRepository;

  private static final int BATCH_COUNT = 100;
  private ArrayList<BoxExcelModel> excelList=new ArrayList<>();
  private BoxInfoEntity boxInfoEntity;
  private ArrayList<String> success;
  private ArrayList<String> fail;
  private OperationUtils operationUtils;

  public BoxExcelListener(OperationUtils utils, BoxInfoEntityRepository boxInfoEntityRepository, ArrayList<String> success, ArrayList<String> fail) {
    this.boxInfoEntityRepository = boxInfoEntityRepository;
    this.operationUtils = utils;
    this.success = success;
    this.fail = fail;
  }


  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {

    // 这里也要保存数据，确保最后遗留的数据也存储到数据库
    saveData();
    LOG.info("All data complete！");
  }

  /**
   * 加上存储数据库
   */
  @Transactional
  private void saveData() {
    //调用mapper插入数据库
    for (BoxExcelModel model : excelList){
      if(CommonUtils.isNotNull(model.getCpuId()) && model.getCpuId().matches("[0-9a-fA-F]+")){
        String boxUUID=operationUtils.string2SHA256("eulixspace-productid-"+model.getCpuId());
        HashMap<String, String> mapExcel = operationUtils.jsonToObject(operationUtils.objectToJson(model), HashMap.class);
        String btid=operationUtils.string2SHA256("eulixspace-btid-"+model.getCpuId()).substring(0, 16);
        mapExcel.put("boxUuid", boxUUID);
        mapExcel.put("btid",btid);
        mapExcel.put("boxqrcode", "https://ao.space/?btid="+btid);
        mapExcel.put("btidHash", operationUtils.string2SHA256("eulixspace-"+btid));
        if(boxInfoEntityRepository.findByBoxUUID(boxUUID).isPresent()){
          try{
            boxInfoEntityRepository.update(operationUtils.objectToJson(mapExcel), boxUUID);
            success.add(boxUUID);
          } catch (Exception ie){
            fail.add(boxUUID);
          }
        } else {
          try {
            boxInfoEntity = new BoxInfoEntity();
            boxInfoEntity.setBoxUUID(boxUUID);
            boxInfoEntity.setExtra(operationUtils.objectToJson(mapExcel));
            boxInfoEntityRepository.persist(boxInfoEntity);
            success.add(boxUUID);
          } catch (PersistenceException exception) {
            LOG.errorv(exception, "box info save failed");
            fail.add(boxUUID);
          }
        }
      }
    }
  }

  @Override
  public void invoke(BoxExcelModel boxExcelModel, AnalysisContext analysisContext) {
    excelList.add(boxExcelModel);
    // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
    if (excelList.size() >= BATCH_COUNT) {
      saveData();
      // 存储完成清理 list
      excelList.clear();
    }
  }
}
