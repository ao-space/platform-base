package xyz.eulix.platform.services.push.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.push.dto.DeviceTokenReq;
import xyz.eulix.platform.services.push.dto.DeviceTokenRes;
import xyz.eulix.platform.services.push.dto.PushMessage;
import xyz.eulix.platform.services.push.repository.PushTokenEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PushService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    PushTokenEntityRepository pushTokenEntityRepository;

    public DeviceTokenRes registryDeviceToken(DeviceTokenReq deviceTokenReq) {
        return null;
    }

    public Boolean pushMessage(PushMessage pushMessage) {
        return true;
    }
}
