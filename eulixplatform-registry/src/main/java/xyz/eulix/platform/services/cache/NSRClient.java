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
import xyz.eulix.platform.common.support.log.Logged;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Network Server Route 缓存客户端（Redis）
 *
 * For more information:
 * <a href="https://quarkus.io/guides/redis">https://quarkus.io/guides/redis</a>.
 *
 * proxy 和 network-server 增加协议头字段，暂定为 EID
 *
 * 域名到 network-server 和 EID 的路由在 redis 中的存储格式暂定为： key 为 "NSR-"前缀 + 用户的域名；
 * value 分两段，用","分隔，第一段为目标域名，第二段为 network-client 配对的id。
 *
 */
@ApplicationScoped
public class NSRClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    public static final String NSR_PREV = "NSR-";

    @Inject
    RedisClient redisClient;

    public NSRoute getNSRoute(String userDomain) {
        Response response = redisClient.get(NSR_PREV + userDomain);
        NSRoute nsRoute = new NSRoute(NSR_PREV + userDomain, response != null ? response.toString() : null);
        LOG.debugv("get NSRoute success, key:{0}, value:{1}", nsRoute.getUserDomain(), nsRoute.getNetworkInfo());
        return nsRoute;
    }

    public void setNSRoute(NSRoute nsRoute) {
        redisClient.set(Arrays.asList(nsRoute.getUserDomain(), nsRoute.getNetworkInfo()));
        LOG.debugv("set NSRoute success, key:{0}, value:{1}", nsRoute.getUserDomain(), nsRoute.getNetworkInfo());
    }

    public void setNSRoute(String userDomain, String serverAddr, String clientId) {
        setNSRoute(new NSRoute(NSR_PREV + userDomain, serverAddr + "," + clientId));
    }
    @Logged
    public void setRedirect(String userDomain, String serverAddr, String clientId, String newUserDomain, NSRRedirectStateEnum redirectState) {
        setNSRoute(new NSRoute(NSR_PREV + userDomain, serverAddr + "," + clientId + "," + newUserDomain + "," + redirectState.getState()));
    }
    public void expireNSRoute(String userDomain, String expireSeconds) {
        redisClient.expire(NSR_PREV + userDomain, expireSeconds);
        LOG.debugv("expire NSRoute success, key:{0}, expire time:{1}s", NSR_PREV + userDomain, expireSeconds);
    }
}
