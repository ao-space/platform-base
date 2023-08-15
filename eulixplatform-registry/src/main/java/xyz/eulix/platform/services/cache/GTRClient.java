/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.services.cache;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.log.Logged;
import xyz.eulix.platform.common.support.serialization.OperationUtils;
import xyz.eulix.platform.services.config.ApplicationProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Network Server Route 缓存客户端（Redis）
 *
 * For more information:
 * <a href="https://quarkus.io/guides/redis">https://quarkus.io/guides/redis</a>.
 *
 */
@ApplicationScoped
public class GTRClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    public static final String SEPARATOR = ":";

    public static final String GTR_PREV = "GTR:";

    public static final String GTR_PREV_CLIENTS = "SpaceUUIDs:";

    public static final String GTR_PREV_APP_TOKENS = "AppDomains:";

    @Inject
    RedisClient redisClient;

    @Inject
    OperationUtils operationUtils;

    @Inject
    ApplicationProperties properties;

    public GTRouteBasic getGTRouteBasic(String subdomain) {
        String key = GTR_PREV + subdomain;
        Response response = redisClient.get(key);
        GTRouteBasic gtRouteBasic = new GTRouteBasic(subdomain, response != null ? getNetworkBasic(response.toString()) : null);
        LOG.debugv("get GTRouteBasic success, key:{0}, value:{1}", key, gtRouteBasic.getNetworkBasic());
        return gtRouteBasic;
    }

    public NetworkBasic getNetworkBasic(String networkBasicStr) {
        return operationUtils.jsonToObject(networkBasicStr, NetworkBasic.class);
    }

    public void setGTRouteBasic(GTRouteBasic gtRouteBasic) {
        String key = GTR_PREV + gtRouteBasic.getSubdomain();
        String value = operationUtils.objectToJson(gtRouteBasic.getNetworkBasic());
        redisClient.set(Arrays.asList(key, value));
        LOG.debugv("set GTRouteBasic success, key:{0}, value:{1}", key, value);
    }

    public void setGTRouteClients(GTRouteClients gtRouteClients) {
        String key = GTR_PREV_CLIENTS + gtRouteClients.getBoxUUID() + SEPARATOR + gtRouteClients.getUserId();
        if (CommonUtils.isNullOrEmpty(gtRouteClients.getClientUUIDs())) {
            LOG.warnv("set GTRouteClients fail due to clientUUIDs is empty, key:{0}", key);
            return;
        }
        List<String> lists = new ArrayList<>();
        lists.add(key);
        lists.addAll(gtRouteClients.getClientUUIDs());
        redisClient.sadd(lists);
        LOG.debugv("set GTRouteClients success, key:{0}, value:{1}", key, gtRouteClients.getClientUUIDs());
    }

    @Logged
    public void setRedirect(String subdomain, String serverAddr, String clientId, String newUserDomain, NSRRedirectStateEnum redirectState,
                            String boxUUID, String userId) {
        NetworkBasic networkBasic = new NetworkBasic(serverAddr, clientId, newUserDomain, redirectState.getState(), boxUUID, userId);
        GTRouteBasic gtRouteBasic = new GTRouteBasic(subdomain, networkBasic);
        setGTRouteBasic(gtRouteBasic);
    }

    public void addClientUUID(String boxUUID, String userId, String clientUUID) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        List<String> lists = new ArrayList<>();
        lists.add(key);
        lists.add(clientUUID);
        redisClient.sadd(lists);
        redisClient.persist(key);
        LOG.debugv("add GTRouteClient success, key:{0}, value:{1}", key, clientUUID);
    }

    public void removeClientUUID(String boxUUID, String userId, String clientUUID){
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        List<String> lists = new ArrayList<>();
        lists.add(key);
        lists.add(clientUUID);
        redisClient.srem(lists);
        redisClient.persist(key);
        LOG.debugv("remove GTRouteClient success, key:{0}, value:{1}", key, clientUUID);
    }

    public void addAppToken(String boxUUID, String appToken) {
        String key = GTR_PREV_APP_TOKENS + boxUUID;
        List<String> lists = new ArrayList<>();
        lists.add(key);
        lists.add(appToken);
        redisClient.sadd(lists);
        redisClient.persist(key);
        LOG.debugv("add GTRouteAppToken success, key:{0}, value:{1}", key, appToken);
    }

    public void removeAppToken(String boxUUID, String appToken){
        String key = GTR_PREV_APP_TOKENS + boxUUID;
        List<String> lists = new ArrayList<>();
        lists.add(key);
        lists.add(appToken);
        redisClient.srem(lists);
        redisClient.persist(key);
        LOG.debugv("remove GTRouteAppToken success, key:{0}, value:{1}", key, appToken);
    }

    public void expireGTRouteBasic(String subdomain, String expireSeconds) {
        String key = GTR_PREV + subdomain;
        redisClient.expire(key, expireSeconds);
        LOG.debugv("expire GTRouteBasic success, key:{0}, expire time:{1}s", key, expireSeconds);
    }

    public void expireGTRouteClients(String boxUUID, String userId, String expireSeconds) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        redisClient.expire(key, expireSeconds);
        LOG.debugv("expire GTRouteClients success, key:{0}, expire time:{1}s", key, expireSeconds);
    }

    public void expireGTRouteAppTokens(String boxUUID, String expireSeconds) {
        String key = GTR_PREV_APP_TOKENS + boxUUID;
        redisClient.expire(key, expireSeconds);
        LOG.debugv("expire GTRouteAppTokens success, key:{0}, expire time:{1}s", key, expireSeconds);
    }

    public void clearGTRouteBasic(List<String> subdomains) {
        if (subdomains.isEmpty()) {
            LOG.debugv("subdomains is empty");
            return;
        }
        List<String> keys = subdomains.stream().map(subdomain -> GTR_PREV + subdomain).collect(Collectors.toList());
        Integer count = redisClient.del(keys).toInteger();
        LOG.debugv("clear NSRouteBasic success, keys:{0}, count:{1}", keys, count);
    }

    public void clearGTRouteClients(String boxUUID, String userId) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        Integer count = redisClient.del(Arrays.asList(key)).toInteger();
        LOG.debugv("clear NSRouteClients success, key:{0}, count:{1}", key, count);
    }

    public void clearNSRouteAppTokens(List<String> boxUUIDs) {
        if (boxUUIDs.isEmpty()) {
            LOG.debugv("boxUUIDs is empty");
            return;
        }
        List<String> keys = boxUUIDs.stream().map(boxUUID -> GTR_PREV_APP_TOKENS + boxUUID).collect(Collectors.toList());
        Integer count = redisClient.del(keys).toInteger();
        LOG.debugv("clear NSRouteAppTokens success, keys:{0}, count:{1}", keys, count);
    }
}
