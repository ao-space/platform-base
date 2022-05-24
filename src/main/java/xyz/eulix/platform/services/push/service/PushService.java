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
import xyz.eulix.platform.services.support.boundary.push.IOSNotification;
import xyz.eulix.platform.services.support.boundary.push.PushClient;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidBroadcast;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidFilecast;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidListcast;
import xyz.eulix.platform.services.support.boundary.push.ios.IOSBroadcast;
import xyz.eulix.platform.services.support.boundary.push.ios.IOSFilecast;
import xyz.eulix.platform.services.support.boundary.push.ios.IOSListcast;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
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

    private String appKeyAndroid;

    private String appSecretAndroid;

    private String appKeyIOS;

    private String appSecretIOS;


    @PostConstruct
    void init() {
        appKeyAndroid = applicationProperties.getUPushAppKey();
        appSecretAndroid = applicationProperties.getUPushAppSecret();
        appKeyIOS = applicationProperties.getUPushAppKeyIOS();
        appSecretIOS = applicationProperties.getUPushAppSecretIOS();
    }

    @Transactional
    public DeviceTokenRes registryDeviceToken(DeviceTokenReq deviceTokenReq) {
        // 查询问卷是否存在
        PushTokenEntity pushTokenEntity = deviceTokenReqToEntity(deviceTokenReq);
        Optional<PushTokenEntity> pushTokenEntityOp = pushTokenEntityRepository.findByClientUUID(deviceTokenReq.getClientUUID());
        if (pushTokenEntityOp.isEmpty()) {
            pushTokenEntityRepository.persist(pushTokenEntity);
        } else if (!pushTokenEntityOp.get().getDeviceToken().equals(deviceTokenReq.getDeviceToken())) {
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
        // 参数校验
        if (CommonUtils.isNullOrEmpty(pushMessage.getBoxUUID())) {
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.boxUUID");
        }
        if (CommonUtils.isNullOrEmpty(pushMessage.getBoxRegKey())) {
            throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.boxRegKey");
        }
        // 校验盒子合法性
        registryService.hasBoxNotRegistered(pushMessage.getBoxUUID(), pushMessage.getBoxRegKey());

        switch (MessageTypeEnum.fromValue(pushMessage.getType())) {
            case CLIENTCAST:
                // 参数校验
                List<PushMessage.UserIdAndClientUUID> clientUUIDS = pushMessage.getClientUUIDs();
                if (CommonUtils.isNullOrEmpty(clientUUIDS)) {
                    throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.clientUUIDs");
                }
                // 列播消息
                return messageClientcast(pushMessage);
            default:
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.type");
        }
    }

    public Boolean broadcastMessage(PushMessage pushMessage) {
        switch (MessageTypeEnum.fromValue(pushMessage.getType())) {
            case BROADCAST:
                // 广播消息
                return messageBroadcast(pushMessage);
            default:
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.type");
        }
    }

    // 列播消息：android & ios
    private Boolean messageClientcast(PushMessage pushMessage) {
        // 获取 device token 列表
        Set<String> clientUUIDs = new HashSet<>();
        List<PushMessage.UserIdAndClientUUID> userIdAndClientUUIDS = pushMessage.getClientUUIDs();
        userIdAndClientUUIDS.forEach(userIdAndClientUUID -> {
            if (!clientUUIDs.contains(userIdAndClientUUID.getClientUUID())) {
                clientUUIDs.add(userIdAndClientUUID.getClientUUID());
            }
        });
        if (CommonUtils.isNullOrEmpty(clientUUIDs)) {
            LOG.infov("ClientUUID is empty");
            return true;
        }
        List<PushTokenEntity> pushTokenEntities = pushTokenEntityRepository.findByClientUUIDs(clientUUIDs);
        // 分类 android & ios 的device token
        List<String> androidTokens = new ArrayList<>();
        List<String> iosTokens = new ArrayList<>();
        pushTokenEntities.stream().forEach(pushTokenEntity -> {
            if (DeviceTypeEnum.IOS.getName().equals(pushTokenEntity.getDeviceType())) {
                iosTokens.add(pushTokenEntity.getDeviceToken());
            } else {
                androidTokens.add(pushTokenEntity.getDeviceToken());
            }
        });
        Boolean succAndroid = true;
        Boolean succIOS = true;
        // 列播消息：android
        if (!CommonUtils.isNullOrEmpty(androidTokens)) {
            succAndroid = androidClientcast(pushMessage, androidTokens);
        }
        // 列播消息：ios
        if (!CommonUtils.isNullOrEmpty(iosTokens)) {
            succIOS = iosClientcast(pushMessage, iosTokens);
        }
        LOG.infov("Clientcast result, android:{0}, ios:{1}", succAndroid ? "success" : "fail", succIOS ? "success" : "fail");
        return succAndroid && succIOS;
    }

    private Boolean androidClientcast(PushMessage pushMessage, List<String> deviceTokens) {
        AndroidListcast androidListcast = pushMessageToAndroidListcast(pushMessage, deviceTokens);
        return pushClient.sendMessage(androidListcast);
    }

    private Boolean iosClientcast(PushMessage pushMessage, List<String> deviceTokens) {
        IOSListcast iosListcast = pushMessageToIOSListcast(pushMessage, deviceTokens);
        return pushClient.sendMessage(iosListcast);
    }

    // 广播消息：android & ios
    public Boolean messageBroadcast(PushMessage pushMessage) {
        // 广播消息：android
        Boolean succAndroid = androidBroadcast(pushMessage);
        // 广播消息：ios
        Boolean succIOS = iosBroadcast(pushMessage);
        LOG.infov("Broadcast result, android:{0}, ios:{1}", succAndroid ? "success" : "fail", succIOS ? "success" : "fail");
        return succAndroid && succIOS;
    }

    public Boolean androidBroadcast(PushMessage pushMessage) {
        AndroidBroadcast androidBroadcast = pushMessageToAndroidBroadcast(pushMessage);
        return pushClient.sendMessage(androidBroadcast);
    }

    public Boolean iosBroadcast(PushMessage pushMessage) {
        IOSBroadcast iosBroadcast = pushMessageToIOSBroadcast(pushMessage);
        return pushClient.sendMessage(iosBroadcast);
    }

    private AndroidBroadcast pushMessageToAndroidBroadcast(PushMessage pushMessage) {
        AndroidBroadcast broadcast = new AndroidBroadcast(appKeyAndroid, appSecretAndroid);
        pushMessageToAndroidNotification(pushMessage, broadcast);
        return broadcast;
    }

    private IOSBroadcast pushMessageToIOSBroadcast(PushMessage pushMessage) {
        IOSBroadcast broadcast = new IOSBroadcast(appKeyIOS, appSecretIOS);
        pushMessageToIOSNotification(pushMessage, broadcast);
        return broadcast;
    }

    private IOSListcast pushMessageToIOSListcast(PushMessage pushMessage, List<String> deviceTokens) {
        IOSListcast listcast = new IOSListcast(appKeyIOS, appSecretIOS);
        listcast.setDeviceTokens(String.join(",", deviceTokens));
        pushMessageToIOSNotification(pushMessage, listcast);
        return listcast;
    }

    private AndroidListcast pushMessageToAndroidListcast(PushMessage pushMessage, List<String> deviceTokens) {
        AndroidListcast listcast = new AndroidListcast(appKeyAndroid, appSecretAndroid);
        listcast.setDeviceTokens(String.join(",", deviceTokens));
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

    private void pushMessageToIOSNotification(PushMessage pushMessage, IOSNotification iosNotification) {
        // 发送消息描述
        iosNotification.setDescription(pushMessage.getDescription());

        // payload
        MessagePayload payload = pushMessage.getPayload();
        // payload aps
        MessagePayloadBody payloadBody = payload.getBody();
        isParamEmptyThrowEx(payloadBody.getText(), "pushMessage.payload.body.text");
        isParamEmptyThrowEx(payloadBody.getTitle(), "pushMessage.payload.body.title");
        iosNotification.setAlert(payloadBody.getTitle(), null, payloadBody.getText());
        // payload extra
        if (CommonUtils.isNotNull(payload.getExtra())) {
            payload.getExtra().forEach(iosNotification::setCustomizedField);
        }

        // prod mode
        Boolean prodMode = applicationProperties.getUPushProdMode();
        iosNotification.setProductionMode(prodMode);

        // policy
        MessagePolicy policy = pushMessage.getPolicy();
        if (policy != null && !CommonUtils.isNullOrEmpty(policy.getStartTime())) {
            if (!CommonUtils.isLocalDateTimeFormat(policy.getStartTime())) {
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.policy.startTime");
            }
            iosNotification.setStartTime(policy.getStartTime());
        }
        if (policy != null && !CommonUtils.isNullOrEmpty(policy.getExpireTime())) {
            if (!CommonUtils.isLocalDateTimeFormat(policy.getExpireTime())) {
                throw new ServiceOperationException(ServiceError.INPUT_PARAMETER_ERROR, "pushMessage.policy.expireTime");
            }
            iosNotification.setExpireTime(policy.getExpireTime());
        }
    }

    /**
     * 通知全部的管理员的绑定手机
     *
     * @return 是否成功
     */
    public Boolean adminFilecast(PushMessage pushMessage) {
        // 查询全部的管理员的绑定手机
        Set<String> clientUUIDs = registryService.getAdminBindClients();
        if (CommonUtils.isNullOrEmpty(clientUUIDs)) {
            LOG.infov("ClientUUID is empty");
            return true;
        }
        // 分类 android & ios 的device token
        List<String> androidTokens = new ArrayList<>();
        List<String> iosTokens = new ArrayList<>();
        pushTokenEntityRepository.findByClientUUIDs(clientUUIDs).stream().forEach(pushTokenEntity -> {
            if (DeviceTypeEnum.IOS.getName().equals(pushTokenEntity.getDeviceType())) {
                iosTokens.add(pushTokenEntity.getDeviceToken());
            } else {
                androidTokens.add(pushTokenEntity.getDeviceToken());
            }
        });
        Boolean succAndroid = true;
        Boolean succIOS = true;
        // 文件播消息：android
        if (!CommonUtils.isNullOrEmpty(androidTokens)) {
            AndroidFilecast androidFilecast = pushMessageToAndroidFilecast(pushMessage);
            succAndroid =  pushClient.batchSendMessage(appKeyAndroid, androidFilecast, androidTokens);
        }
        // 文件播消息：ios
        if (!CommonUtils.isNullOrEmpty(iosTokens)) {
            IOSFilecast iosFilecast = pushMessageToIOSFilecast(pushMessage);
            succIOS = pushClient.batchSendMessageIOS(appKeyIOS, iosFilecast, iosTokens);
        }
        LOG.infov("Filecast result, android:{0}, ios:{1}", succAndroid ? "success" : "fail", succIOS ? "success" : "fail");
        return succAndroid && succIOS;
    }

    private AndroidFilecast pushMessageToAndroidFilecast(PushMessage pushMessage) {
        AndroidFilecast filecast = new AndroidFilecast(appKeyAndroid, appSecretAndroid);
        pushMessageToAndroidNotification(pushMessage, filecast);
        return filecast;
    }

    private IOSFilecast pushMessageToIOSFilecast(PushMessage pushMessage) {
        IOSFilecast filecast = new IOSFilecast(appKeyIOS, appSecretIOS);
        pushMessageToIOSNotification(pushMessage, filecast);
        return filecast;
    }
}
