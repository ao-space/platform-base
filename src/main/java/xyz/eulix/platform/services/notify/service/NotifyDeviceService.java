package xyz.eulix.platform.services.notify.service;


import xyz.eulix.platform.services.notify.dto.NotifyDeviceInfo;
import xyz.eulix.platform.services.notify.entity.NotifyDevice;
import xyz.eulix.platform.services.notify.repository.NotifyDeviceRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static xyz.eulix.platform.services.notify.entity.NotifyDevice.State.ACTIVE;

@ApplicationScoped
public class NotifyDeviceService {
    @Inject
    NotifyDeviceRepository deviceRepository;

    @Transactional
    public NotifyDevice registerDevice(NotifyDeviceInfo info) {
        NotifyDevice device = deviceRepository.deviceByDeviceId(info.getDeviceId());
        if (device != null) {
            fillData(info, device);
            return device;
        }
        device = new NotifyDevice();
        fillData(info, device);
        deviceRepository.registerDevice(device);
        return device;
    }

    private void fillData(NotifyDeviceInfo info, NotifyDevice device) {
        device.setPlatform(info.getPlatform().toLowerCase());
        device.setDeviceId(info.getDeviceId());
        device.setClientUUID(info.getClientUUID());
        device.setDeviceToken(info.getDeviceToken());
        device.setClientRegKey(info.getClientRegKey());
        device.setState(ACTIVE.getValue());
    }

    @Transactional
    public List<NotifyDevice> activeDevicesByClientUUID(String clientUUID) {
        return deviceRepository.activeDevicesByClientUUID(clientUUID);
    }

    @Transactional
    public NotifyDevice deviceByDeviceId(String deviceId) {
        return deviceRepository.deviceByDeviceId(deviceId);
    }

    @Transactional
    public boolean deviceExist(String deviceId) {
        return deviceByDeviceId(deviceId) != null;
    }

    @Transactional
    public void deviceOnline(String deviceID) {
        deviceRepository.markDeviceActive(deviceID);
    }

    @Transactional
    public void deviceOffline(String deviceId) {
        deviceRepository.markDeviceInactive(deviceId);
    }
}
