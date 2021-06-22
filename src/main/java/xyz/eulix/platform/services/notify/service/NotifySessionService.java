package xyz.eulix.platform.services.notify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import xyz.eulix.platform.services.notify.dto.NotifySessionInfo;
import xyz.eulix.platform.services.notify.entity.NotifyMessage;
import xyz.eulix.platform.services.notify.repository.NotifyMessageRepository;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.OperationUtils;
import xyz.eulix.platform.services.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/session/{deviceId}")
@ApplicationScoped
public class NotifySessionService {

    @Inject
    NotifyMessageRepository messageRepository;

    @Inject
    NotifyDeviceService deviceService;
    Map<String, NotifySessionInfo> sessions = new ConcurrentHashMap<>();
    @Inject
    OperationUtils utils;
    private Timer timer;

    @OnMessage
    public void onMessage(String message, @PathParam("deviceId") String deviceId) {
        ///TODO
        /// quarkus.websocket.dispatch-to-worker=true
        try {
            HashMap request =
                    new ObjectMapper().readValue(message, HashMap.class);
            Method method = Method.methodOf((String) request.get("method"));
            String messageId = (String) request.get("messageId");
            HashMap parameters = (HashMap) request.get("parameters");
            switch (method) {
                case LOGIN:
                    onLogin(deviceId, messageId, parameters);
                    break;
                case PING:
                    onPing(deviceId);
                    break;
                case ACK:
                    onACK(messageId, parameters);
                    break;
                case QUERY:
                    onQuery(deviceId, messageId, parameters);
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("deviceId") String deviceId) {
        setupTimer();
        logged("Device [" + deviceId + "] connected");
        sessions.put(deviceId, new NotifySessionInfo(session, deviceId));
    }

    @OnClose
    public void onClose(Session session, @PathParam("deviceId") String deviceId) {
        clearDevice(deviceId);
        logged("Device [" + deviceId + "] close");
    }

    @OnError
    public void onError(Session session, @PathParam("deviceId") String deviceId, Throwable throwable) {
        clearDevice(deviceId);
        logged("Device [" + deviceId + "] left on error: " + throwable);
    }

    @Logged
    private void onLogin(String deviceId, String messageId, HashMap parameters) {
        NotifySessionInfo info = sessions.get(deviceId);
        String platform = (String) parameters.get("platform");
        String clientUUID = (String) parameters.get("clientUUID");
        if (clientUUID != null && platform != null) {
            if (!deviceService.deviceExist(deviceId)) {
                String msg = "Unknown device [" + deviceId + "], please register device first!";
                notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", -1, "msg", msg)), info.getSession());
                throw new IllegalArgumentException(msg);
            }
            info.setClientUUID(clientUUID);
            deviceService.deviceOnline(deviceId);
            logged("deviceId [" + deviceId + "]" + " logged in with clientUUID : [" +
                    clientUUID + "] on platform : [" + platform + "]");
            notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", 0)), info.getSession());
        } else {
            String msg = "clientUUID && platform must not be null";
            notify(resultBuilder(Method.LOGIN, messageId, ImmutableMap.of("code", -1, "msg", msg)), info.getSession());
            throw new IllegalArgumentException(msg);
        }
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
            logged("Device [" + info.getDeviceId() + "] left on expired");
            sessions.remove(info.getDeviceId());
        });
    }

    private void onPing(String deviceId) {
        NotifySessionInfo info = sessions.get(deviceId);
        info.markAsActive();
        logged("Device [" + info.getDeviceId() + "] ping");
        notify(resultBuilder(Method.PONG), info.getSession());
    }

    private void onQuery(String deviceId, String messageId, HashMap parameters) {
        NotifySessionInfo info = sessions.get(deviceId);
        int page = (int) parameters.getOrDefault("page", 0);
        int pageSize = (int) parameters.getOrDefault("pageSize", 10);
        Long count = messageRepository.offlineMessageCount(info.getClientUUID());
        List<NotifyMessage> offlineMessages = messageRepository.listOfflineMessage(info.getClientUUID(), page, pageSize);
        PageListResult<? extends NotifyMessage> result = PageListResult.of(offlineMessages, PageInfo.of(
                count, page, pageSize
        ));
        notify(resultBuilder(Method.QUERY, messageId, result), info.getSession());
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
        messageMap.put("method", method.raw());
        if (result != null) {
            messageMap.put("result", utils.jsonToObject(utils.objectToJson(result), HashMap.class));
        }
        return utils.objectToJson(messageMap);
    }

    private void onACK(String messageId, HashMap parameters) {
        if (messageId != null) {
            messageRepository.markMessageSent(messageId);
        }
        if (parameters != null) {
            ArrayList<String> list = (ArrayList<String>) parameters.get("list");
            list.forEach(id -> messageRepository.markMessageSent(id));
        }
    }

    @Transactional
    private void clearDevice(String deviceId) {
        sessions.remove(deviceId);
        deviceService.deviceOffline(deviceId);
    }

    private void logged(String message) {
        System.out.println("broadcast : " + message);
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
                System.out.println("Unable to send message: " + result.getException());
            }
        });
    }

    public String messageBuilder(NotifyMessage message) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("title", message.getTitle());
        parameters.put("body", message.getBody());
        parameters.put("messageId", message.getMessageId());
        parameters.put("extParameters", utils.jsonToObject(message.getExtParameters(), HashMap.class));
        return messageBuilder(Method.PUSH, parameters);
    }

    public String messageBuilder(Method method, HashMap<String, Object> parameters) {
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("method", method);
        messageMap.put("parameters", parameters);
        return utils.objectToJson(messageMap);
    }

    public boolean online(String deviceId) {
        return sessions.get(deviceId) != null;
    }

    public boolean notify(String message, String deviceId) {
        NotifySessionInfo info = sessions.get(deviceId);
        if (info != null) {
            info.getSession().getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        }
        return info != null;
    }

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

        public String raw() {
            return value;
        }
    }
}
