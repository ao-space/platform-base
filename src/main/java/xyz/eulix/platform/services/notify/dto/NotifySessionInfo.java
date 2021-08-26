package xyz.eulix.platform.services.notify.dto;


import lombok.Getter;
import lombok.Setter;

import javax.websocket.Session;

public class NotifySessionInfo {

    @Getter
    private final Session session;
    ///登录超时时间，超过这个时间，则自动断开连接
    private final long loginExpiredIntervalMillis = 5 * 1000L;
    ///心跳超时时间，超过这个时间，则自动断开连接
    private final long heartbeatExpiredIntervalMillis = 60 * 1000L;
    @Getter
    @Setter
    private String deviceId;
    @Getter
    @Setter
    private String clientUUID;
    private long activeTimestampMillis;

    public NotifySessionInfo(Session session) {
        this.session = session;
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
