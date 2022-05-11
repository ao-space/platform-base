package xyz.eulix.platform.services.push.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.push.dto.*;
import xyz.eulix.platform.services.push.entity.PushTokenEntity;
import xyz.eulix.platform.services.push.repository.PushTokenEntityRepository;
import xyz.eulix.platform.services.registry.service.RegistryService;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.boundary.push.AfterOpenAction;
import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;
import xyz.eulix.platform.services.support.boundary.push.PushClient;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidBroadcast;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidListcast;
import xyz.eulix.platform.services.support.serialization.OperationUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class PushService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    PushTokenEntityRepository pushTokenEntityRepository;

    @Inject
    PushClient pushClient;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    RegistryService registryService;

    @Inject
    OperationUtils operationUtils;

    private String appKey;

    private String appSecret;

    @PostConstruct
    void init() {
        appKey = applicationProperties.getUPushAppKey();
        appSecret = applicationProperties.getUPushAppSecret();
    }

    @Transactional
    public DeviceTokenRes registryDeviceToken(DeviceTokenReq deviceTokenReq) {
        // 查询问卷是否存在
        PushTokenEntity pushTokenEntity = deviceTokenReqToEntity(deviceTokenReq);
        Optional<PushTokenEntity> pushTokenEntityOp = pushTokenEntityRepository.findByClientUUID(deviceTokenReq.getClientUUID());
        if (pushTokenEntityOp.isEmpty()) {
            pushTokenEntityRepository.persist(pushTokenEntity);
        } else if (!pushTokenEntityOp.get().getDeviceToken().equals(deviceTokenReq.getDeviceToken())){
            pushTokenEntityRepository.updateByClientUUID(deviceTokenReq.getClientUUID(), deviceTokenReq.getDeviceToken(), deviceTokenReq.getExtra());
            pushTokenEntity = pushTokenEntityOp.get();
            pushTokenEntity.setDeviceToken(deviceTokenReq.getDeviceToken());
            pushTokenEntity.setExtra(deviceTokenReq.getExtra());
        }
        return pushTokenEntityToRes(deviceTokenReq.getBoxUUID(), deviceTokenReq.getUserId(), pushTokenEntity);
    }

    private PushTokenEntity deviceTokenReqToEntity(DeviceTokenReq deviceTokenReq) {
        PushTokenEntity pushTokenEntity = new PushTokenEntity();
        pushTokenEntity.setClientUUID(deviceTokenReq.getClientUUID());
        pushTokenEntity.setDeviceToken(deviceTokenReq.getDeviceToken());
        pushTokenEntity.setDeviceType(deviceTokenReq.getDeviceType());
        pushTokenEntity.setExtra(deviceTokenReq.getExtra());
        return pushTokenEntity;
    }

    private DeviceTokenRes pushTokenEntityToRes(String boxUUID, String userId, PushTokenEntity pushTokenEntity) {
        return DeviceTokenRes.of(boxUUID,
                userId,
                pushTokenEntity.getClientUUID(),
                pushTokenEntity.getDeviceToken(),
                pushTokenEntity.getDeviceType(),
                pushTokenEntity.getExtra(),
                pushTokenEntity.getCreatedAt(),
                pushTokenEntity.getUpdatedAt());
    }

    public Boolean pushMessage(PushMessage pushMessage) {
        // 校验盒子合法性
        registryService.hasBoxNotRegistered(pushMessage.getBoxUUID(), pushMessage.getBoxRegKey());

        switch (MessageTypeEnum.fromValue(pushMessage.getType())) {
            case CLIENTCAST:
                // 参数校验
                List<PushMessage.UserIdAndClientUUID> clientUUIDS = pushMessage.getClientUUIDs();
                if (CommonUtils.isNullOrEmpty(clientUUIDS)) {
                    throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.clientUUIDs");
                }
                return messageClientcast(pushMessage);
            default:
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.type");
        }
    }

    public Boolean broadcastMessage(PushMessage pushMessage) {
        switch (MessageTypeEnum.fromValue(pushMessage.getType())) {
            case BROADCAST:
                return messageBroadcast(pushMessage);
            default:
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.type");
        }
    }

    private Boolean messageClientcast(PushMessage pushMessage) {
        AndroidListcast androidListcast = pushMessageToAndroidListcast(pushMessage);
        return pushClient.sendMessage(androidListcast);
    }

    private Boolean messageBroadcast(PushMessage pushMessage) {
        AndroidBroadcast androidBroadcast = pushMessageToAndroidBroadcast(pushMessage);
        return pushClient.sendMessage(androidBroadcast);
    }

    private AndroidBroadcast pushMessageToAndroidBroadcast(PushMessage pushMessage) {
        AndroidBroadcast broadcast = new AndroidBroadcast(appKey, appSecret);
        pushMessageToAndroidNotification(pushMessage, broadcast);
        return broadcast;
    }


    private AndroidListcast pushMessageToAndroidListcast(PushMessage pushMessage) {
        AndroidListcast listcast = new AndroidListcast(appKey, appSecret);
        // 获取 device token 列表
        List<String> clientUUIDs = new ArrayList<>();
        List<PushMessage.UserIdAndClientUUID> userIdAndClientUUIDS = pushMessage.getClientUUIDs();
        userIdAndClientUUIDS.forEach(userIdAndClientUUID -> {
            if (!clientUUIDs.contains(userIdAndClientUUID.getClientUUID())) {
                clientUUIDs.add(userIdAndClientUUID.getClientUUID());
            }
        });
        List<PushTokenEntity> pushTokenEntities = pushTokenEntityRepository.findByClientUUIDs(clientUUIDs);
        String deviceTokens = pushTokenEntities.stream().map(PushTokenEntity::getDeviceToken).collect(Collectors.joining(","));
        listcast.setDeviceTokens(deviceTokens);
        pushMessageToAndroidNotification(pushMessage, listcast);
        return listcast;
    }

    private void isParamEmptyThrowEx(String param, String paramName) {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, paramName);
        }
    }

    private void pushMessageToAndroidNotification(PushMessage pushMessage, AndroidNotification androidNotification) {
        // 发送消息描述
        androidNotification.setDescription(pushMessage.getDescription());

        // payload
        MessagePayload payload = pushMessage.getPayload();
        // payload display
        androidNotification.setDisplayType(payload.getDisplayType());
        // payload body
        MessagePayloadBody payloadBody = payload.getBody();
        switch (DisplayTypeEnum.fromValue(payload.getDisplayType())) {
            case NOTIFICATION:
                isParamEmptyThrowEx(payloadBody.getText(), "pushMessage.payload.body.text");
                androidNotification.setText(payloadBody.getText());
                isParamEmptyThrowEx(payloadBody.getTitle(), "pushMessage.payload.body.title");
                androidNotification.setTitle(payloadBody.getTitle());
                isParamEmptyThrowEx(payloadBody.getAfterOpen(), "pushMessage.payload.body.afterOpen");
                switch (AfterOpenAction.fromValue(payloadBody.getAfterOpen())) {
                    case GO_APP:
                        androidNotification.goAppAfterOpen();
                        break;
                    case GO_URL:
                        isParamEmptyThrowEx(payloadBody.getUrl(), "pushMessage.payload.body.url");
                        androidNotification.goUrlAfterOpen(payloadBody.getUrl());
                        break;
                    case GO_ACTIVITY:
                        isParamEmptyThrowEx(payloadBody.getActivity(), "pushMessage.payload.body.activity");
                        androidNotification.goActivityAfterOpen(payloadBody.getActivity());
                        break;
                    case GO_CUSTOM:
                        isParamEmptyThrowEx(payloadBody.getCustom(), "pushMessage.payload.body.custom");
                        androidNotification.goCustomAfterOpen(payloadBody.getCustom());
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                break;
            case MESSAGE:
                isParamEmptyThrowEx(payloadBody.getCustom(), "pushMessage.payload.body.custom");
                androidNotification.goCustomAfterOpen(payloadBody.getCustom());
                break;
            default:
                throw new UnsupportedOperationException();
        }
        // payload extra
        if (CommonUtils.isNotNull(payload.getExtra())) {
            payload.getExtra().forEach(androidNotification::setExtraField);
        }

        // prod mode
        Boolean prodMode = applicationProperties.getUPushProdMode();
        androidNotification.setProductionMode(prodMode);

        // policy
        MessagePolicy policy = pushMessage.getPolicy();
        if (policy != null && !CommonUtils.isNullOrEmpty(policy.getStartTime())) {
            if (!CommonUtils.isLocalDateTimeFormat(policy.getStartTime())) {
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.policy.startTime");
            }
            androidNotification.setStartTime(policy.getStartTime());
        }
        if (policy != null && !CommonUtils.isNullOrEmpty(policy.getExpireTime())) {
            if (!CommonUtils.isLocalDateTimeFormat(policy.getExpireTime())) {
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.policy.expireTime");
            }
            androidNotification.setExpireTime(policy.getExpireTime());
        }

        // channelProperties
        ChannelProperties channelProperties = pushMessage.getChannelProperties();
        androidNotification.setChannelActivity(channelProperties.getChannelActivity());
    }
}
