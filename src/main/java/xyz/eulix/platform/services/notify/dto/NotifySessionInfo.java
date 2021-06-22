package xyz.eulix.platform.services.notify.dto;


import lombok.Getter;
import lombok.Setter;

import javax.websocket.Session;

public class NotifySessionInfo {

    @Getter
    private final Session session;

    @Getter
    private final String deviceId;
    ///登录超时时间，超过这个时间，则自动断开连接
    private final long loginExpiredIntervalMillis = 5 * 1000;
    ///心跳超时时间，超过这个时间，则自动断开连接
    private final long heartbeatExpiredIntervalMillis = 60 * 1000;
    @Getter
    @Setter
    private String clientUUID;
    private long activeTimestampMillis;

    public NotifySessionInfo(Session session, String deviceId) {
        this.session = session;
        this.deviceId = deviceId;
        markAsActive();
    }

    public void markAsActive() {
        activeTimestampMillis = System.currentTimeMillis();
    }

    public boolean isActive() {
        long interval = System.currentTimeMillis() - activeTimestampMillis;
        if (deviceId == null) {
            return interval < loginExpiredIntervalMillis;
        }
        return interval < heartbeatExpiredIntervalMillis;
    }
}
