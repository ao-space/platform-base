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

package xyz.eulix.platform.services.provider;

import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.provider.inf.RegistryProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ProviderFactory {
    private static final String PROVIDER_NAME = "default";
    private static final Map<String, RegistryProvider> map = new ConcurrentHashMap<>();

    @Inject
    ApplicationProperties properties;

    public static void putRegisterProvider(String name, RegistryProvider registryProvider){
        map.put(name, registryProvider);
    }

    public RegistryProvider getRegistryProvider() {
        String name = properties.getRegisterProviderName();
        if (map.containsKey(name)) {
            return map.get(name);
        } else {
            return map.get(PROVIDER_NAME);
        }
    }
}
