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
import java.util.Map;

/**
 * Mock NSRClient for tests - uses in-memory storage instead of Redis.
 */
@Mock
@ApplicationScoped
public class MockNSRClient extends NSRClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    private final Map<String, String> nsRouteCache = new HashMap<>();

    @Override
    public NSRoute getNSRoute(String userDomain) {
        String networkInfo = nsRouteCache.get(NSR_PREV + userDomain);
        LOG.debugv("[Mock] get NSRoute, userDomain:{0}, found:{1}", userDomain, networkInfo != null);
        return new NSRoute(NSR_PREV + userDomain, networkInfo);
    }

    @Override
    public void setNSRoute(NSRoute nsRoute) {
        nsRouteCache.put(nsRoute.getUserDomain(), nsRoute.getNetworkInfo());
        LOG.debugv("[Mock] set NSRoute, key:{0}, value:{1}", nsRoute.getUserDomain(), nsRoute.getNetworkInfo());
    }

    @Override
    public void setNSRoute(String userDomain, String serverAddr, String clientId) {
        setNSRoute(new NSRoute(NSR_PREV + userDomain, serverAddr + "," + clientId));
    }

    @Override
    public void setRedirect(String userDomain, String serverAddr, String clientId, String newUserDomain, NSRRedirectStateEnum redirectState) {
        setNSRoute(new NSRoute(NSR_PREV + userDomain, serverAddr + "," + clientId + "," + newUserDomain + "," + redirectState.getState()));
    }

    @Override
    public void expireNSRoute(String userDomain, String expireSeconds) {
        LOG.debugv("[Mock] expire NSRoute (no-op), userDomain:{0}", userDomain);
    }

    // Utility method to clear all caches (useful for test cleanup)
    public void clearAll() {
        nsRouteCache.clear();
    }
}
