package xyz.eulix.platform.services.notify.service;

import xyz.eulix.platform.services.notify.dto.NotifyMessageInfo;
import xyz.eulix.platform.services.notify.entity.NotifyDevice;
import xyz.eulix.platform.services.notify.entity.NotifyMessage;
import xyz.eulix.platform.services.notify.repository.NotifyMessageRepository;
import xyz.eulix.platform.services.notify.support.apns.IOSPusher;
import xyz.eulix.platform.services.support.OperationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static xyz.eulix.platform.services.notify.entity.NotifyMessage.State.SENDING;
import static xyz.eulix.platform.services.notify.entity.NotifyMessage.State.SENT;


@ApplicationScoped
public class NotifyMessageService {
    @Inject
    NotifyMessageRepository messageRepository;

    @Inject
    NotifySessionService sessionService;

    @Inject
    OperationUtils utils;

    IOSPusher pusher = new IOSPusher();

    @Transactional
    public NotifyMessage createMessage(NotifyMessageInfo info) {
        NotifyMessage message = new NotifyMessage();
        {
            message.setState(SENDING.getValue());
            message.setTitle(info.getTitle());
            message.setBody(info.getBody());
            message.setClientUUID(info.getClientUUID());
            message.setExtParameters(utils.objectToJson(info.getExtParameters()));
            message.setMessageId(randomUUID().toString());
        }
        return message;
    }

    @Transactional
    public boolean pushMessage(NotifyMessage message, List<NotifyDevice> devices) {
        messageRepository.saveMessage(message);
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        devices.forEach(device -> {
            pushMessageToDevice(message, device);
        });
        return true;
    }

    private boolean pushMessageToDevice(NotifyMessage message, NotifyDevice device) {
        System.out.println("push message : " + message + "to device : " + device);
        boolean pushToIOSResult = true;
        if (device.iOS()) {
            List<String> deviceTokens = new ArrayList<>(1);
            deviceTokens.add(device.getDeviceToken());
            pushToIOSResult = pusher.push(deviceTokens, message.getTitle(), message.getBody());
        }

        boolean pushToSocket = sessionService.online(device.getDeviceId());
        sessionService.notify(sessionService.messageBuilder(message), device.getDeviceId());
        return pushToIOSResult && pushToSocket;
    }


}
