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

import io.quarkus.test.Mock;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock GTRClient for tests - uses in-memory storage instead of Redis.
 */
@Mock
@ApplicationScoped
public class MockGTRClient extends GTRClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    private final Map<String, NetworkBasic> gtRouteBasicCache = new HashMap<>();
    private final Map<String, Set<String>> gtRouteClientsCache = new HashMap<>();
    private final Map<String, Set<String>> gtRouteAppTokensCache = new HashMap<>();

    @Override
    public GTRouteBasic getGTRouteBasic(String subdomain) {
        NetworkBasic networkBasic = gtRouteBasicCache.get(GTR_PREV + subdomain);
        LOG.debugv("[Mock] get GTRouteBasic, subdomain:{0}, found:{1}", subdomain, networkBasic != null);
        return new GTRouteBasic(subdomain, networkBasic);
    }

    @Override
    public void setGTRouteBasic(GTRouteBasic gtRouteBasic) {
        String key = GTR_PREV + gtRouteBasic.getSubdomain();
        gtRouteBasicCache.put(key, gtRouteBasic.getNetworkBasic());
        LOG.debugv("[Mock] set GTRouteBasic, key:{0}", key);
    }

    @Override
    public void setGTRouteClients(GTRouteClients gtRouteClients) {
        String key = GTR_PREV_CLIENTS + gtRouteClients.getBoxUUID() + SEPARATOR + gtRouteClients.getUserId();
        if (gtRouteClients.getClientUUIDs() == null || gtRouteClients.getClientUUIDs().isEmpty()) {
            return;
        }
        gtRouteClientsCache.computeIfAbsent(key, k -> new HashSet<>())
                .addAll(gtRouteClients.getClientUUIDs());
        LOG.debugv("[Mock] set GTRouteClients, key:{0}", key);
    }

    @Override
    public void setRedirect(String subdomain, String serverAddr, String clientId, String newUserDomain,
                            NSRRedirectStateEnum redirectState, String boxUUID, String userId) {
        NetworkBasic networkBasic = new NetworkBasic(serverAddr, clientId, newUserDomain, redirectState.getState(), boxUUID, userId);
        GTRouteBasic gtRouteBasic = new GTRouteBasic(subdomain, networkBasic);
        setGTRouteBasic(gtRouteBasic);
    }

    @Override
    public void addClientUUID(String boxUUID, String userId, String clientUUID) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        gtRouteClientsCache.computeIfAbsent(key, k -> new HashSet<>()).add(clientUUID);
        LOG.debugv("[Mock] add GTRouteClient, key:{0}, value:{1}", key, clientUUID);
    }

    @Override
    public void removeClientUUID(String boxUUID, String userId, String clientUUID) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        Set<String> clients = gtRouteClientsCache.get(key);
        if (clients != null) {
            clients.remove(clientUUID);
        }
        LOG.debugv("[Mock] remove GTRouteClient, key:{0}, value:{1}", key, clientUUID);
    }

    @Override
    public void addAppToken(String boxUUID, String appToken) {
        String key = GTR_PREV_APP_TOKENS + boxUUID;
        gtRouteAppTokensCache.computeIfAbsent(key, k -> new HashSet<>()).add(appToken);
        LOG.debugv("[Mock] add GTRouteAppToken, key:{0}, value:{1}", key, appToken);
    }

    @Override
    public void removeAppToken(String boxUUID, String appToken) {
        String key = GTR_PREV_APP_TOKENS + boxUUID;
        Set<String> tokens = gtRouteAppTokensCache.get(key);
        if (tokens != null) {
            tokens.remove(appToken);
        }
        LOG.debugv("[Mock] remove GTRouteAppToken, key:{0}, value:{1}", key, appToken);
    }

    @Override
    public void expireGTRouteBasic(String subdomain, String expireSeconds) {
        LOG.debugv("[Mock] expire GTRouteBasic (no-op), subdomain:{0}", subdomain);
    }

    @Override
    public void expireGTRouteClients(String boxUUID, String userId, String expireSeconds) {
        LOG.debugv("[Mock] expire GTRouteClients (no-op), boxUUID:{0}, userId:{1}", boxUUID, userId);
    }

    @Override
    public void expireGTRouteAppTokens(String boxUUID, String expireSeconds) {
        LOG.debugv("[Mock] expire GTRouteAppTokens (no-op), boxUUID:{0}", boxUUID);
    }

    @Override
    public void clearGTRouteBasic(List<String> subdomains) {
        for (String subdomain : subdomains) {
            gtRouteBasicCache.remove(GTR_PREV + subdomain);
        }
        LOG.debugv("[Mock] clear GTRouteBasic, subdomains:{0}", subdomains);
    }

    @Override
    public void clearGTRouteClients(String boxUUID, String userId) {
        String key = GTR_PREV_CLIENTS + boxUUID + SEPARATOR + userId;
        gtRouteClientsCache.remove(key);
        LOG.debugv("[Mock] clear GTRouteClients, key:{0}", key);
    }

    @Override
    public void clearNSRouteAppTokens(List<String> boxUUIDs) {
        for (String boxUUID : boxUUIDs) {
            gtRouteAppTokensCache.remove(GTR_PREV_APP_TOKENS + boxUUID);
        }
        LOG.debugv("[Mock] clear NSRouteAppTokens, boxUUIDs:{0}", boxUUIDs);
    }

    // Utility method to clear all caches (useful for test cleanup)
    public void clearAll() {
        gtRouteBasicCache.clear();
        gtRouteClientsCache.clear();
        gtRouteAppTokensCache.clear();
    }
}
