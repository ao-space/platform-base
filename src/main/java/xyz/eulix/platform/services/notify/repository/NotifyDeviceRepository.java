package xyz.eulix.platform.services.notify.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.notify.entity.NotifyDevice;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static xyz.eulix.platform.services.notify.entity.NotifyDevice.State.ACTIVE;
import static xyz.eulix.platform.services.notify.entity.NotifyDevice.State.INACTIVE;


@ApplicationScoped
public class NotifyDeviceRepository implements PanacheRepository<NotifyDevice> {
    /**
     * Register the device.
     *
     * @param device the specified message
     */
    public void registerDevice(NotifyDevice device) {
        this.persist(device);
    }

    /**
     * Returns all active device of the specified clientUUID.
     *
     * @param clientUUID the specified clientUUID
     * @return all active device of the specified clientUUID.
     */
    public List<NotifyDevice> activeDevicesByClientUUID(String clientUUID) {
        return this.list("client_uuid=?1 AND (state=?2 OR platform=?3) ", clientUUID, ACTIVE.getValue(), "ios");
    }

    /**
     * Returns device of the specified deviceId.
     *
     * @param deviceId the specified deviceId
     * @return device of the specified deviceId.
     */
    public NotifyDevice deviceByDeviceId(String deviceId) {
        return this.find("device_id=?1", deviceId).firstResult();
    }

    /**
     * Set the device state to Inactive,
     * it will transfer the device from any state to <tt>INACTIVE</tt>.
     *
     * @param deviceId the specified device id
     */
    public void markDeviceInactive(String deviceId) {
        this.update(
                " state=?1 WHERE deviceId=?2",
                INACTIVE.getValue(), deviceId);
    }

    /**
     * Set the device state to active,
     * it will transfer the device from any state to <tt>ACTIVE</tt>.
     *
     * @param deviceId the specified device id
     */
    public void markDeviceActive(String deviceId) {
        this.update(
                " state=?1 WHERE deviceId=?2",
                ACTIVE.getValue(), deviceId);
    }
}
