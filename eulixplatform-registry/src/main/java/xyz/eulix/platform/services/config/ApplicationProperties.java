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

package xyz.eulix.platform.services.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Global application configuration properties.
 *
 * <p>Impl ref:
 * <a _href=https://quarkus.io/guides/config-reference>https://quarkus.io/guides/config-reference</a>
 */
@ConfigProperties(prefix = "app")
public interface ApplicationProperties {

    @ConfigProperty
    String getVersion();

    @ConfigProperty(name = "registry.subdomain")
    String getRegistrySubdomain();

    @ConfigProperty(name = "registry.box-uuid-policy")
    String getRegistryBoxUUIDPolicy();

    @ConfigProperty(name = "constant.speed-of-sound-in-meter-per-second", defaultValue = "343")
    int getSpeedOfSound();

    @ConfigProperty(name = "display.mach")
    Optional<Integer> getDisplayMach();

    @ConfigProperty(name = "display.unit.name")
    String getDisplayUnitName();

    @ConfigProperty(name = "display.unit.factor")
    BigDecimal getDisplayUnitFactor();

    @ConfigProperty(name = "forceupdate.min-box-version", defaultValue = "0.0.0")
    String getMinBoxVersion();

    @ConfigProperty(name = "forceupdate.min-android-version", defaultValue = "0.0.0")
    String getMinAndroidVersion();

    @ConfigProperty(name = "forceupdate.min-ios-version", defaultValue = "0.0.0")
    String getMinIOSVersion();

    @ConfigProperty(name = "file.location")
    String getFileLocation();

    // OSS
    @ConfigProperty(name = "oss.endpoint")
    String getOSSEndpoint();

    @ConfigProperty(name = "oss.public.domain")
    String getOSSPublicDomain();

    @ConfigProperty(name = "oss.public.path")
    String getOSSPublicPath();

    @ConfigProperty(name = "ali.accesskey.id")
    String getAliAccessKeyId();

    @ConfigProperty(name = "ali.accesskey.secret")
    String  getAliAccessKeySecret();

    @ConfigProperty(name = "oss.bucket.name")
    String getOSSBucketName();

    @ConfigProperty(name = "subdomain.effective-time", defaultValue = "30")
    Integer getSubdomainEffectiveTime();

    @ConfigProperty(name = "upush.appkey.android")
    String getUPushAppKey();

    @ConfigProperty(name = "upush.appsecret.android")
    String getUPushAppSecret();

    @ConfigProperty(name = "upush.prod-mode")
    Boolean getUPushProdMode();

    @ConfigProperty(name = "upush.appkey.ios")
    String getUPushAppKeyIOS();

    @ConfigProperty(name = "upush.appsecret.ios")
    String getUPushAppSecretIOS();

    @ConfigProperty(name = "pkg-mgt.notify", defaultValue = "true")
    Boolean getPkgMgtNotify();

    @ConfigProperty(name = "register.provider.name", defaultValue = "default")
    String getRegisterProviderName();

    @ConfigProperty(name = "api-resources.location")
    String getPlatformApisLocation();

    @ConfigProperty(name = "lock.expire-time", defaultValue = "30")
    Integer getLockExpireTime();

    @ConfigProperty(name = "migration.route.subdomain.expire-time", defaultValue = "259200")
    Integer getSubdomainRedirectTime();
}
