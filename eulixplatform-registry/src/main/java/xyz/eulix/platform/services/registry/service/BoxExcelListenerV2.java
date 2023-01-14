/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.services.registry.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import java.util.ArrayList;
import java.util.HashMap;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.services.registry.dto.registry.BoxFailureInfo;
import xyz.eulix.platform.services.registry.entity.BoxExcelModelV2;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;

public class BoxExcelListenerV2 implements ReadListener<BoxExcelModelV2> {
    private static final Logger LOG = Logger.getLogger("app.log");

    private BoxInfoService boxInfoService;
    private OperationUtils operationUtils;

    private static final int BATCH_COUNT = 100;
    private HashMap<Integer, BoxExcelModelV2> excelList = new HashMap<>();
    private ArrayList<String> success;
    private ArrayList<String> failure;
    private ArrayList<BoxFailureInfo> fail;

    public BoxExcelListenerV2(BoxInfoService boxInfoService, OperationUtils utils, ArrayList<String> success, ArrayList<String> failure, ArrayList<BoxFailureInfo> fail) {
        this.boxInfoService = boxInfoService;
        this.operationUtils = utils;
        this.success = success;
        this.failure = failure;
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
    private void saveData() {
        //调用mapper插入数据库
        for (Integer number : excelList.keySet()) {
            BoxExcelModelV2 model = excelList.get(number);
            if (model.isNUllOrEmpty()) {
                continue;
            } else if (CommonUtils.isNotNull(model.getCpuId()) && model.getCpuId().matches("[0-9a-fA-F]+")) {
                String boxUUID = operationUtils.string2SHA256("eulixspace-productid-" + model.getCpuId());
                model.setBoxUuid(boxUUID);
                if (!CommonUtils.isNullOrEmpty(model.getSnNumber())) {
                    String btid = operationUtils.string2SHA256(model.getSnNumber()).substring(0, 16);
                    String boxqrcode = "https://ao.space/?sn=" + model.getSnNumber();
                    String btidHash = operationUtils.string2SHA256("eulixspace-" + btid);
                    model.setBtid(btid);
                    model.setBoxqrcode(boxqrcode);
                    model.setBtidHash(btidHash);
                }
                String boxPubKey = model.getBoxPubKey();
                String authType;
                if (CommonUtils.isNullOrEmpty(boxPubKey)) {
                    authType = AuthTypeEnum.BOX_UUID.getName();
                } else {
                    authType = AuthTypeEnum.BOX_PUB_KEY.getName();
                    model.setBoxPubKey(null);
                }
                if (!boxInfoService.upsertBoxInfoV2(boxUUID, null, model, authType, boxPubKey,
                        success, failure)) {
                    fail.add(BoxFailureInfo.of(String.valueOf(number), boxUUID));
                }
            } else {
                LOG.warnv("cpuid is invalid, rowNum:{0}, cpuid:{1}", number, model.getCpuId());
                fail.add(BoxFailureInfo.of(String.valueOf(number), ""));
            }
        }
    }

    @Override
    public void invoke(BoxExcelModelV2 boxExcelModel, AnalysisContext analysisContext) {
        excelList.put(analysisContext.readRowHolder().getRowIndex()+1, boxExcelModel);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (excelList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            excelList.clear();
        }
    }
}
