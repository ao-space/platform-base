package xyz.eulix.platform.services.notify.support.apns;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import xyz.eulix.platform.services.support.log.Logged;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class IOSPusher {
    private static final String topic = "xyz.eulix.space";
    private static final Semaphore semaphore = new Semaphore(10000);
    private static ApnsClient developmentApnsClient = null;
    private static ApnsClient productionApnsClient = null;

    @Logged
    public boolean push(final List<String> deviceTokens,
                        String alertTitle,
                        String alertBody,
                        HashMap<String, Object> extParameters,
                        boolean sandbox) {
        ApnsClient apnsClient = null;
        if (sandbox) {
            if (developmentApnsClient == null) {
                try {
                    EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    InputStream inputStream = classloader.getResourceAsStream("development.p12");
                    developmentApnsClient = new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                            .setClientCredentials(inputStream, "2021")
                            .setConcurrentConnections(4).setEventLoopGroup(eventLoopGroup).build();
                } catch (Exception e) {
                    throw new IllegalStateException("ios get pushy apns client failed!");
                }
            }
            apnsClient = developmentApnsClient;
        } else {
            if (productionApnsClient == null) {
                try {
                    EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    InputStream inputStream = classloader.getResourceAsStream("production.p12");
                    productionApnsClient = new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                            .setClientCredentials(inputStream, "2021")
                            .setConcurrentConnections(4).setEventLoopGroup(eventLoopGroup).build();
                } catch (Exception e) {
                    throw new IllegalStateException("ios get pushy apns client failed!");
                }
            }
            apnsClient = productionApnsClient;
        }

        long startTime = System.currentTimeMillis();

        long total = deviceTokens.size();
        final CountDownLatch latch = new CountDownLatch(deviceTokens.size());
        final AtomicLong successCnt = new AtomicLong(0);
        long startPushTime = System.currentTimeMillis();
        for (String deviceToken : deviceTokens) {
            final SimpleApnsPushNotification pushNotification;

            {
                final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
                payloadBuilder.setAlertTitle(alertTitle);
                payloadBuilder.setAlertBody(alertBody);
                if (extParameters != null) {
                    extParameters.forEach(payloadBuilder::addCustomProperty);
                }

                final String payload = payloadBuilder.build();
                final String token = TokenUtil.sanitizeTokenString(deviceToken);

                pushNotification = new SimpleApnsPushNotification(token, topic, payload);
            }

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new IllegalStateException("ios push get semaphore failed, deviceToken:{" + deviceToken + "}");
            }
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                    sendNotificationFuture = apnsClient.sendNotification(pushNotification);

            try {
                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                        sendNotificationFuture.get();

                if (pushNotificationResponse.isAccepted()) {
                    successCnt.incrementAndGet();
                    System.out.println("Push notification accepted by APNs gateway.");
                } else {
                    System.out.println("Notification rejected by the APNs gateway: " +
                            pushNotificationResponse.getRejectionReason());

                    pushNotificationResponse.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                        System.out.println("\t…and the token is invalid as of " + timestamp);
                    });
                }
                latch.countDown();
                semaphore.release();
            } catch (final ExecutionException e) {
                System.err.println("ExecutionException : Failed to send push notification.");
                e.printStackTrace();
            } catch (final InterruptedException e) {
                System.err.println("InterruptedException : Failed to send push notification.");
                e.printStackTrace();
            }

            sendNotificationFuture.whenComplete((response, cause) -> {
                if (response != null) {
                    // Handle the push notification response as before from here.
                } else {
                    // Something went wrong when trying to send the notification to the
                    // APNs server. Note that this is distinct from a rejection from
                    // the server, and indicates that something went wrong when actually
                    // sending the notification or waiting for a reply.
                    cause.printStackTrace();
                }
            });
            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("ios push latch await failed!");
                e.printStackTrace();
                throw new IllegalStateException("ios push latch await failed!");
            }
        }
        long endPushTime = System.currentTimeMillis();
        System.out.println("pushMessage success. total : [" + total + "] success : [" + (successCnt.get()) +
                "], total cost = " + (endPushTime - startTime) + ", pushCost = " + (endPushTime - startPushTime));
        return total == successCnt.get();
    }
}