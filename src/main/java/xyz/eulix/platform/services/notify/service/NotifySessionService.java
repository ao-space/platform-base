package xyz.eulix.platform.services.notify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.notify.dto.NotifySessionInfo;
import xyz.eulix.platform.services.notify.entity.NotifyMessage;
import xyz.eulix.platform.services.notify.repository.NotifyMessageRepository;
import xyz.eulix.platform.services.support.OperationUtils;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/{placeholder}")
@ApplicationScoped
public class NotifySessionService {

    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    NotifyMessageRepository messageRepository;

    @Inject
    NotifyDeviceService deviceService;
    Map<String, NotifySessionInfo> sessions = new ConcurrentHashMap<>();
    Map<String, String> deviceIdMappingToSessionId = new ConcurrentHashMap<>();
    @Inject
    OperationUtils utils;
    private Timer timer;

    @OnMessage
    public void onMessage(Session session, String message) {
        /// TODO
        /// quarkus.websocket.dispatch-to-worker=true
        /// logged("Session [" + session.getId() + "] onMessage : " + message);
        String sessionId = session.getId();
        try {
            HashMap request = new ObjectMapper().readValue(message, HashMap.class);
            Method method = Method.methodOf((String) request.get("method"));
            String messageId = (String) request.get("messageId");
            HashMap parameters = (HashMap) request.get("parameters");
            switch (method) {
                case LOGIN:
                    onLogin(sessions.get(sessionId), messageId, parameters);
                    break;
                case PING:
                    onPing(sessions.get(sessionId));
                    break;
                case QUERY:
                    onQuery(sessions.get(sessionId), messageId, parameters);
                    break;
                case ACK:
                    onACK(messageId, parameters);
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        setupTimer();
        sessions.put(session.getId(), new NotifySessionInfo(session));
        logged("Session [" + session.getId() + "] onOpen");
    }

    @OnClose
    public void onClose(Session session) {
        String deviceId = sessions.get(session.getId()).getDeviceId();
        if (deviceId != null) {
            clearDevice(deviceId);
            logged("Device [" + deviceId + "] close");
        } else {
            logged("Session [" + session.getId() + "] onClose");
        }
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String deviceId = sessions.get(session.getId()).getDeviceId();
        if (deviceId != null) {
            logged("Device [" + deviceId + "] trigger on error: " + throwable);
        }
    }

    @Logged
    private void onLogin(NotifySessionInfo info, String messageId, HashMap parameters) {
        String deviceId = (String) parameters.get("deviceId");
        String platform = (String) parameters.get("platform");
        String clientUUID = (String) parameters.get("clientUUID");
        if (clientUUID != null && platform != null) {
            if (!deviceService.deviceExist(deviceId)) {
                String msg = "Unknown device [" + deviceId + "], please register device first!";
                notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", -1, "msg", msg)), info.getSession());
                clearDevice(deviceId);
                throw new IllegalArgumentException(msg);
            }
            info.setClientUUID(clientUUID);
            info.setDeviceId(deviceId);
            deviceIdMappingToSessionId.put(deviceId, info.getSession().getId());
            deviceService.deviceOnline(deviceId);
            logged("sessionId [" + info.getSession().getId() + "]" + "with deviceId [" + deviceId + "]" + " logged in with clientUUID : [" +
                    clientUUID + "] on platform : [" + platform + "]");
            notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", 0)), info.getSession());
        } else {
            String msg = "clientUUID && platform must not be null";
            notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", -1, "msg", msg)), info.getSession());
            clearDevice(deviceId);
            throw new IllegalArgumentException(msg);
        }
    }

    private void onPing(NotifySessionInfo info) {
        if (info == null) {
            return;
        }
        info.markAsActive();
        logged("Device [" + info.getDeviceId() + "] ping");
        notify(resultBuilder(Method.PONG), info.getSession());
    }

    private void onQuery(NotifySessionInfo info, String messageId, HashMap parameters) {
        int page = (int) parameters.getOrDefault("page", 0);
        int pageSize = (int) parameters.getOrDefault("pageSize", 10);
        Long count = messageRepository.offlineMessageCount(info.getClientUUID());
        List<NotifyMessage> offlineMessages = messageRepository.listOfflineMessage(info.getClientUUID(), page, pageSize);
        PageListResult<? extends NotifyMessage> result = PageListResult.of(offlineMessages, PageInfo.of(
                count, page, pageSize
        ));
        notify(resultBuilder(Method.QUERY, messageId, result), info.getSession());
    }

    @Transactional
    public void onACK(String messageId, HashMap parameters) {
        if (messageId != null) {
            messageRepository.markMessageSent(messageId);
        }
        if (parameters != null) {
            ArrayList<String> list = (ArrayList<String>) parameters.get("list");
            list.forEach(id -> messageRepository.markMessageSent(id));
        }
    }

    public String resultBuilder(Method method) {
        return resultBuilder(method, null);
    }

    public String resultBuilder(Method method, String messageId) {
        return resultBuilder(method, messageId, null);
    }

    public String resultBuilder(Method method, String messageId, Object result) {
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", messageId);
        messageMap.put("method", method.getValue());
        if (result != null) {
            messageMap.put("result", utils.jsonToObject(utils.objectToJson(result), HashMap.class));
        }
        return utils.objectToJson(messageMap);
    }

    public String resultBuilder(NotifyMessage message) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("title", message.getTitle());
        parameters.put("body", message.getBody());
        parameters.put("extParameters", utils.jsonToObject(message.getExtParameters(), HashMap.class));
        return resultBuilder(Method.PUSH, message.getMessageId(), parameters);
    }

    @Transactional
    public void clearDevice(String deviceId) {
        sessions.remove(deviceId);
        deviceService.deviceOffline(deviceId);
    }

    private void setupTimer() {
        if (timer == null) {
            ///开启定时器，1s扫描一次，是否有超时的session
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    clear();
                }
            }, 0, 1000);
        }
    }

    private void clear() {
        List<NotifySessionInfo> expiredList = new ArrayList<>();
        sessions.values().forEach(info -> {
            boolean isActive = info.isActive();
            if (!isActive) {
                expiredList.add(info);
            }
        });
        expiredList.forEach(info -> {
            try {
                String reason;
                if (info.getDeviceId() == null) {
                    reason = "Please login";
                } else {
                    reason = "No beating";
                }
                info.getSession().close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, reason));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (info.getDeviceId() != null) {
                logged("Device [" + info.getDeviceId() + "] left on expired");
                sessions.remove(info.getDeviceId());
            }
        });
    }

    private void logged(String message) {
        LOG.infof("broadcast : " + message);
//        sessions.values().forEach(session -> {
//            session.getSession().getAsyncRemote().sendObject(message, result -> {
//                if (result.getException() != null) {
//                    System.out.println("Unable to send message: " + result.getException());
//                }
//            });
//        });
    }

    private void notify(String message, Session session) {
        session.getAsyncRemote().sendObject(message, result -> {
            if (result.getException() != null) {
                LOG.errorf(("Unable to send message: " + result.getException());
            }
        });
    }


    public boolean online(String deviceId) {
        String sessionId = deviceIdMappingToSessionId.get(deviceId);
        if (sessionId == null) {
            return false;
        }
        return sessions.containsKey(sessionId);
    }

    public boolean notify(String message, String deviceId) {
        String sessionId = deviceIdMappingToSessionId.get(deviceId);
        if (sessionId == null) {
            return false;
        }
        NotifySessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.getSession().getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    LOG.errorf("Unable to send message: " + result.getException());
                }
            });
        }
        return info != null;
    }

    @Getter
    public enum Method {
        LOGIN("login"),
        PING("ping"),
        PONG("pong"),
        PUSH("push"),
        ACK("ack"),
        QUERY("query"),
        ;

        private final String value;

        Method(String value) {
            this.value = value;
        }

        public static Method methodOf(final String value) {
            Optional<Method> any = Arrays.stream(values()).filter(
                    method -> method.value.contentEquals(value)).findAny();

            return any.orElseThrow(
                    () -> new IllegalArgumentException("this value is illegal for message method - " + value));
        }
    }
}
